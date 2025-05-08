import type { Metadata } from "next";
import "./globals.css";
import { MobileNavigation } from "@/components/mobile-navigation"; // Import MobileNavigation

export const metadata: Metadata = {
  title: 'AstroLearn',
  description: 'Created with v0',
  generator: 'v0.dev',
}

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode
}>) {
  return (
    <html lang="en">
      <body className="relative"> {/* Added relative positioning for potential absolute elements inside */}
        {children}
        <MobileNavigation /> {/* Add MobileNavigation here */}
      </body>
    </html>
  );
}
