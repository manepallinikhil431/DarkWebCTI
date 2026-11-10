# Dark Web Cyber Threat Intelligence Analysis

## Project Overview

Dark Web Cyber Threat Intelligence Analysis is a Java desktop application for exploring cybersecurity-related text records from a CSV dataset. It provides a graphical interface for searching records and running text, topic, entity, classification, and time-based analyses.

The application is intended for educational and defensive cybersecurity analysis. Its keyword and risk-scoring results are demonstrations and should not be treated as confirmed threat assessments.

## Features

- **Import CSV data** and display records in a table.
- **Search and filter records** by their visible fields and full text.
- **Analyze selected text** and preprocess it by cleaning text and removing common stop words.
- **Detect predefined threat keywords** and calculate a rule-based demonstration risk score.
- **Analyze records over time** by month and display keyword-match trends in a chart.
- **Export analysis reports** as text files.
- **Create topic clusters** using TF-IDF term weights and K-means.
- **Explore LDA topics** and calculate topic coherence, topic diversity, and silhouette scores.
- **Calculate the Adjusted Rand Index** to compare dataset categories with K-means cluster assignments.
- **Extract entities** such as IP addresses, email addresses, domain names, CVE identifiers, file hashes, and URLs.
- **Evaluate entity extraction** against a manually labeled example.
- **Predict a category** for selected text using a Naive Bayes classifier trained from the dataset’s category values.
- **Measure temporal topic stability** by comparing LDA topics across months.
- **Calculate analyst relevance** from manually entered ratings on a 1–5 scale.

## Technologies

- Java
- Java Swing
- Apache Commons CSV
- Apache NetBeans
- Git and GitHub

## Running the Application

1. Install the JDK version configured for the project.
2. Clone or download this repository.
3. Open the `DarkWebCTI` project in Apache NetBeans.
4. Confirm that the libraries in the project’s `lib` folder are available to the project.
5. Build and run the project from NetBeans.
6. Click **Import Dataset** and select a compatible CSV file.

The CSV dataset should include columns for `id`, `title`, `category`, `created_at`, and `full_text`. Analysis results depend on the quality and completeness of the data.

## Project Structure

- `src/darkwebcti/` — application interface and analysis classes
- `lib/` — libraries used by the project
- `nbproject/` — NetBeans project settings
- `build.xml` — Ant build file

## Methodology

The application reads records from a CSV file and displays them in the interface. Text analysis uses predefined keyword rules, TF-IDF, K-means, LDA, entity patterns, and a Naive Bayes classifier. Date fields support monthly summaries, trend charts, and topic stability comparisons. Results can be reviewed in the application and, where available, exported as reports.

## Limitations

- Keyword matching may miss threats described with different words.
- A keyword match does not prove that an attack occurred.
- The risk score uses demonstration rules and is not a validated security assessment.
- Naive Bayes predictions depend on the dataset’s category labels and training examples.
- Entity evaluation uses a manually labeled example; it does not establish accuracy across all datasets.
- Analyst relevance results depend on ratings entered by the user.
- Results depend on the quality, completeness, and permitted use of the input data.

## Ethical Use

Use only datasets you are authorized to access and analyze. Follow the dataset’s license and applicable privacy requirements. Do not use this application to facilitate unauthorized access or cybercrime.

## Author

Nikhil Manepalli

## Repository

[DarkWebCTI on GitHub](https://github.com/manepallinikhil431/DarkWebCTI)
