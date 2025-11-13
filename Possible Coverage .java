// ---------------------------------------------------
// CONDITION 4 (Corrected):
// Include ONLY those values from POSSIBLE_VALIDATION
// that are NOT present in SCENARIO_INFO column (token-based match).
// ---------------------------------------------------
else {

    StringBuilder sb = new StringBuilder();

    // Split possible validations
    String[] validations = possibleVal.split(",");

    // Normalize SCENARIO_INFO into comparable tokens
    // Remove hyphens, underscores, spaces, convert to upper
    String scenarioNormalized = scenarioInfo
            .replace("-", "")
            .replace("_", "")
            .replace(" ", "")
            .toUpperCase();

    for (String v : validations) {

        if (v == null) continue;

        // Clean/normalize the validation token similarly
        String originalVal = v.trim();                    // final text to print
        String valNorm = originalVal
                .replace("-", "")
                .replace("_", "")
                .replace(" ", "")
                .toUpperCase();

        // Match token-to-token
        if (!scenarioNormalized.contains(valNorm)) {
            sb.append(originalVal.toUpperCase()).append("\n");
        }
    }

    output = sb.toString().trim();
}
