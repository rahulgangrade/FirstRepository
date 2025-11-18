else {

    StringBuilder sb = new StringBuilder();

    if (possibleVal == null) possibleVal = "";
    if (scenarioInfo == null) scenarioInfo = "";

    // Build normalized scenario string including any "after-colon" tokens
    String scenarioUp = scenarioInfo.toUpperCase();

    // Extract tokens after colon(s) in scenarioInfo (take first token after each colon)
    StringBuilder afterColonParts = new StringBuilder();
    String[] scenarioColonSplit = scenarioUp.split(":");
    if (scenarioColonSplit.length > 1) {
        for (int i = 1; i < scenarioColonSplit.length; i++) {
            String part = scenarioColonSplit[i].trim();
            if (!part.isEmpty()) {
                // take only the first whitespace-separated token after colon
                String[] tok = part.split("\\s+");
                if (tok.length > 0 && !tok[0].isEmpty()) {
                    afterColonParts.append(" ").append(tok[0]);
                }
            }
        }
    }

    // Combine and normalize scenario text (remove hyphens/underscores/spaces and uppercase)
    String scenarioCombined = scenarioUp + " " + afterColonParts.toString();
    String scenarioNormalized = scenarioCombined
            .replace("-", "")
            .replace("_", "")
            .replace(" ", "")
            .toUpperCase();

    // Split possible validations
    String[] validations = possibleVal.split(",");

    for (String v : validations) {

        if (v == null) continue;

        String originalVal = v.trim();
        if (originalVal.isEmpty()) continue;

        // If token contains ':', consider only part after first colon; else whole token
        String consider = originalVal;
        if (originalVal.contains(":")) {
            String[] parts = originalVal.split(":", 2);
            if (parts.length == 2 && parts[1] != null && !parts[1].trim().isEmpty()) {
                consider = parts[1].trim();
            } else {
                consider = parts[0].trim();
            }
        }

        // Normalize the considered piece for comparison
        String valNorm = consider
                .replace("-", "")
                .replace("_", "")
                .replace(" ", "")
                .toUpperCase();

        // If NOT present in scenarioNormalized, include original POSSIBLE_VALIDATION (uppercased)
        if (!scenarioNormalized.contains(valNorm)) {
            sb.append(originalVal.toUpperCase()).append("\n");
        }
    }

    output = sb.toString().trim();
}
