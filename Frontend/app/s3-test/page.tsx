"use client";

import { useState, useRef } from "react";
import axios from "axios";
import axiosInstance from "@/lib/axiosInstance"; // For getting the presigned URL
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Loader2 } from "lucide-react";

export default function S3TestPage() {
  const [selectedFile, setSelectedFile] = useState<File | null>(null);
  const [isLoading, setIsLoading] = useState(false);
  const [statusMessage, setStatusMessage] = useState<string>("");
  const [imageUrl, setImageUrl] = useState<string | null>(null);
  const fileInputRef = useRef<HTMLInputElement>(null);

  const handleFileChange = (event: React.ChangeEvent<HTMLInputElement>) => {
    const file = event.target.files?.[0];
    if (file) {
      setSelectedFile(file);
      setStatusMessage(`Selected file: ${file.name}`);
      setImageUrl(null); // Clear previous image URL
    }
  };

  const handleUpload = async () => {
    if (!selectedFile) {
      setStatusMessage("Please select a file first.");
      return;
    }

    setIsLoading(true);
    setStatusMessage("Requesting pre-signed URL...");
    setImageUrl(null);

    try {
      // 1. Get pre-signed URL from backend
      // Expecting { uploadUrl: string, key: string }
      const response = await axiosInstance.get<{ uploadUrl: string; key: string }>("/generate-upload-url");
      const presignedUrlData = response.data;
      setStatusMessage(`Got pre-signed URL: ${presignedUrlData.uploadUrl.substring(0, 70)}...`);
      console.log("Pre-signed URL Data:", presignedUrlData);

      if (!presignedUrlData || !presignedUrlData.uploadUrl) {
        throw new Error("Invalid pre-signed URL data received.");
      }
      const actualUploadUrl = presignedUrlData.uploadUrl;

      // 2. Upload file to S3 using the pre-signed URL
      setStatusMessage(`Uploading ${selectedFile.name} (Type: ${selectedFile.type}) to S3...`);
      console.log(`Attempting PUT to: ${actualUploadUrl}`);
      console.log(`File type: ${selectedFile.type}`);

      // Use plain axios for the PUT request to S3
      await axios.put(actualUploadUrl, selectedFile, {
        headers: {
          "Content-Type": selectedFile.type,
          // Avoid sending Authorization or other custom headers from axiosInstance
        },
      });

      const finalImageUrl = actualUploadUrl.split("?")[0];
      setStatusMessage(`Upload successful! Image URL: ${finalImageUrl}`);
      setImageUrl(finalImageUrl);
      console.log("Upload successful. Final URL:", finalImageUrl);

    } catch (error: any) {
      console.error("S3 Upload Error:", error);
      let errorDetails = error.message;
      if (error.response) {
        console.error("Axios Error Response Status:", error.response.status);
        console.error("Axios Error Response Headers:", error.response.headers);
        // Attempt to parse XML error response from S3 if available
        try {
            const errorText = await new Response(error.response.data).text();
            console.error("Axios Error Response Data (S3 XML):", errorText);
            errorDetails += ` | Status: ${error.response.status} | S3 Error: Check console for XML details.`;
        } catch (parseError) {
            console.error("Could not parse error response data.");
            errorDetails += ` | Status: ${error.response.status} | Data: (Could not parse)`;
        }
      } else {
         errorDetails += " | No response received from server.";
      }
      setStatusMessage(`Upload failed: ${errorDetails}`);
      setImageUrl(null);
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="container mx-auto p-8 text-white min-h-screen bg-gray-900">
      <h1 className="text-2xl font-bold mb-6">S3 Upload Test Page</h1>
      <div className="space-y-4 max-w-lg bg-gray-800 p-6 rounded-lg border border-gray-700">
        <div>
          <label htmlFor="file-upload" className="block text-sm font-medium mb-2 text-gray-300">
            Choose File to Upload
          </label>
          <Input
            id="file-upload"
            type="file"
            ref={fileInputRef}
            onChange={handleFileChange}
            className="block w-full text-sm text-gray-300 file:mr-4 file:py-2 file:px-4 file:rounded-md file:border file:border-gray-600 file:text-sm file:font-semibold file:bg-gray-700 file:text-gray-300 hover:file:bg-gray-600 cursor-pointer"
          />
        </div>
        <Button onClick={handleUpload} disabled={!selectedFile || isLoading} className="bg-indigo-600 hover:bg-indigo-700">
          {isLoading ? <Loader2 className="mr-2 h-4 w-4 animate-spin" /> : null}
          {isLoading ? "Uploading..." : "Upload Test File to S3"}
        </Button>
        {statusMessage && (
          <div className="mt-4 p-4 bg-gray-700 border border-gray-600 rounded">
            <p className="text-sm font-mono break-words whitespace-pre-wrap">{statusMessage}</p>
          </div>
        )}
        {imageUrl && (
          <div className="mt-4">
            <p className="text-gray-300 mb-2">Uploaded Image Preview:</p>
            {/* Use next/image for optimized loading if desired, but standard img is simpler for direct URL */}
            <img src={imageUrl} alt="Uploaded content" className="max-w-full h-auto mt-2 border border-gray-600 rounded" />
          </div>
        )}
      </div>
    </div>
  );
}
