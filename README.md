# Dark Web Cyber Threat Intelligence Analysis

A Java Swing desktop application for exploring cybersecurity text records from CSV datasets.

## Repository Structure

- `Dataset/` — CSV datasets for analysis
- `Documentation/` — project documentation
- `Implementation/DarkWebCTI/` — NetBeans Java application
- `Report/` — project report

## Run the Application

1. Clone this repository.
2. In Apache NetBeans, open the project located in `Implementation/DarkWebCTI`.
3. Run the project.
4. Click **Import Dataset** and select one of the CSV files in `Dataset/`.

## Features

- Search and explore imported records.
- Preprocess text and identify predefined threat keywords.
- Calculate demonstration risk scores and monthly threat trends.
- Run TF-IDF, K-means, and LDA topic analysis.
- Extract entities such as IP addresses, domains, CVE identifiers, hashes, and URLs.
- Use Naive Bayes to predict a record category.
- Display and export analysis reports.

## Limitations and Responsible Use

Keyword matches and risk scores are educational demonstrations; they do not confirm that an attack occurred or provide a validated security assessment. Use only datasets you are authorized to analyze and share.

## Author

Nikhil Manepalli
