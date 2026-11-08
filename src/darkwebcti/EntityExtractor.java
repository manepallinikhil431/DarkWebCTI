
package darkwebcti;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EntityExtractor {

    private static final Pattern URL_PATTERN = Pattern.compile(
        "https?://[^\\s\\\"'<>]+",
        Pattern.CASE_INSENSITIVE
    );

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}",
        Pattern.CASE_INSENSITIVE
    );

    private static final Pattern IPV4_PATTERN = Pattern.compile(
        "\\b(?:25[0-5]|2[0-4]\\d|1?\\d?\\d)(?:\\.(?:25[0-5]|2[0-4]\\d|1?\\d?\\d)){3}\\b"
    );

    private static final Pattern CVE_PATTERN = Pattern.compile(
        "\\bCVE-\\d{4}-\\d{4,}\\b",
        Pattern.CASE_INSENSITIVE
    );

    private static final Pattern HASH_PATTERN = Pattern.compile(
        "\\b(?:[a-fA-F0-9]{32}|[a-fA-F0-9]{40}|[a-fA-F0-9]{64})\\b"
    );

    private static final Pattern DOMAIN_PATTERN = Pattern.compile(
        "\\b(?:[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?\\.)+[a-zA-Z]{2,}\\b"
    );

    public Map<String, List<String>> extractEntities(String text) {

        Map<String, List<String>> entities =
            new LinkedHashMap<>();

        entities.put("IP Addresses", new ArrayList<>());
        entities.put("Email Addresses", new ArrayList<>());
        entities.put("Domain Names", new ArrayList<>());
        entities.put("CVE Identifiers", new ArrayList<>());
        entities.put("File Hashes", new ArrayList<>());
        entities.put("URLs", new ArrayList<>());

        if (text == null || text.trim().isEmpty()) {
            return entities;
        }

        // Extract URLs
        extractMatches(
            URL_PATTERN, text, entities.get("URLs")
        );

        // Extract email addresses
        extractMatches(
            EMAIL_PATTERN, text, entities.get("Email Addresses")
        );

        // Extract IP addresses
        extractMatches(
            IPV4_PATTERN, text, entities.get("IP Addresses")
        );

        // Extract CVE identifiers
        extractMatches(
            CVE_PATTERN, text, entities.get("CVE Identifiers")
        );

        // Extract file hashes
        extractMatches(
            HASH_PATTERN, text, entities.get("File Hashes")
        );

        // Extract domain names
        List<String> domains = entities.get("Domain Names");

        extractMatches(DOMAIN_PATTERN, text, domains);

        // Remove domains already present in URLs or emails
        domains.removeIf(domain ->
            entities.get("URLs").stream()
                .anyMatch(url -> url.contains(domain))
            ||
            entities.get("Email Addresses").stream()
                .anyMatch(email -> email.endsWith("@" + domain))
        );

        return entities;
    }

    private void extractMatches(
            Pattern pattern,
            String text,
            List<String> results) {

        Matcher matcher = pattern.matcher(text);

        while (matcher.find()) {

            String value = matcher.group();

            // Remove common trailing punctuation
            value = value.replaceAll("[.,;:!?]+$", "");

            if (!results.contains(value)) {
                results.add(value);
            }
        }
    }
}
