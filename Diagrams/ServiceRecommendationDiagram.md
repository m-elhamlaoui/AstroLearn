flowchart TD
 subgraph subGraph0["Step 1: Identify Positive Interactions"]
        B_DB1[("ReadingHistoryRepo: findReadArticleIdsByUserId")]
        B{"Fetch Positive Interaction IDs"}
        B_DB2[("ArticleVoteRepo: findUpvotedArticleIdsByUserId")]
        C["Combine Read and Upvoted IDs with positiveInteractionArticleIds"]
  end
 subgraph subGraph1["Step 2: Get Preferred Tags"]
        E_DB[("ArticleRepo: findAllById")]
        E{"Step 2: Extract Preferred Tags"}
        F["Extract Tag Names from Articles with preferredTags"]
  end
 subgraph subGraph2["Step 3: Personalized Search"]
        H_DB[("ArticleRepo: findByTagsAndExcludeIdsAndAuthorOrderByScore")]
        H{"Step 3: Find Tag-Based Candidates"}
        I["Get Candidate Articles List"]
  end
 subgraph subGraph3["Step 4a: Supplement Logic"]
        L1["Calculate Needed Count"]
        L{"Step 4a: Supplement Needed"}
        L2["Calculate Exclusions of positiveInteractionIds and candidateIds"]
        L3[("ArticleRepo: findTopScoringArticlesExcludingAuthorAndIds")]
        L4["Get Popular Supplement Articles"]
        M["Combine Candidates and Supplements"]
  end
 subgraph subGraph4["Fallback Path"]
        FB1_DB[("ArticleRepo: findTopScoringArticlesExcludingAuthor")]
        FB1["Fallback Path 1"]
        FB2["Get Popular Fallback Articles"]
        K["Map Fallback List to DTOs"]
  end
    A["Start: getArticleRecommendationsForUser"] --> B
    B --> B_DB1 & B_DB2
    B_DB1 --> C
    B_DB2 --> C
    C --> D{"Interactions Exist?"}
    D -- No --> FB1
    D -- Yes --> E
    E --> E_DB
    E_DB --> F
    F --> G{"Tags Found?"}
    G -- No --> FB1
    G -- Yes --> H
    H --> H_DB
    H_DB --> I
    I --> J{"Step 4: Enough Found? candidates.size >= count"} & M
    J -- Yes --> K
    J -- No --> L
    L --> L1 & L2
    L2 --> L3
    L3 --> L4
    L4 --> M
    M --> K
    FB1 --> FB1_DB
    FB1_DB --> FB2
    FB2 --> K
    K --> Z["End: Return List of ArticleDTO"]
