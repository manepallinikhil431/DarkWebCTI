# Dark Web Cyber Threat Intelligence Analysis

## Project Overview

Dark Web Cyber Threat Intelligence Analysis is a Java desktop application designed to analyze cybersecurity-related text records from a CSV dataset. It uses text preprocessing, predefined keyword matching, simple rule-based risk scoring, and monthly trend analysis to help explore potential threat-related discussions.

## Objectives

- Import and explore cybersecurity-related text records.
- Preprocess selected text for basic analysis.
- Identify predefined cybersecurity keywords.
- Demonstrate rule-based threat risk scoring.
- Examine monthly trends in records and keyword matches.
- Export analysis results to a text report.

## Technologies Used

- Java
- Java Swing for the desktop graphical user interface
- Apache Commons CSV for CSV data processing
- NetBeans IDE
- Git and GitHub for source-code management

## Features

1. **Import Dataset** — load records from a CSV file.
2. **Analyze Data** — display dataset summaries and category counts.
3. **Search and Filter Records** — search the loaded records.
4. **Analyze Selected Text** — examine the text of a selected record.
5. **Preprocess Selected Text** — normalize text and remove common stop words.
6. **Detect Threat Keywords** — identify matches against a predefined keyword list.
7. **Classify Threat Risk** — calculate a demonstration risk score using predefined rules.
8. **Temporal Threat Analysis** — summarize records and keyword matches by month.
9. **View Threat Trend Chart** — display monthly keyword-match trends.
10. **Export Analysis Report** — save analysis results as a text file.

## How to Run

1. Install a compatible Java Development Kit (JDK).
2. Open the `DarkWebCTI` project in Apache NetBeans.
3. Configure the required Apache Commons CSV library and any dependencies used by the project.
4. Make sure the CSV dataset is available locally and is compatible with the application's expected columns.
5. Build and run the project from NetBeans.
6. Use **Import Dataset** to load the CSV file and explore the available analysis features.

## Methodology

The application loads CSV records, processes selected text, checks for predefined cybersecurity keywords, applies a simple rule-based score, and groups records by month for trend visualization. Results can be reviewed in the graphical interface and exported to a text report.

## Limitations

- Keyword matching can miss threats that use different terminology.
- A keyword match does not prove that a cyberattack occurred.
- The risk score is a demonstration rule, not a validated security assessment.
- Results depend on the quality, completeness, and permitted use of the input dataset.
- The application should be used for educational and defensive cybersecurity analysis.

## Ethical Use

Use only datasets that you are authorized to access and analyze. Follow the dataset's licence and applicable privacy requirements. Do not use this application to facilitate unauthorized access or cybercrime.

## Author

Nikhil Manepalli

## Repository

https://github.com/manepallinikhil431/DarkWebCTI
