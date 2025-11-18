{
    StringBuilder sb = new StringBuilder();

    if (possibleVal == null) possibleVal = "";
    if (scenarioInfo == null) scenarioInfo = "";

    // -----------------------
    // Build normalized scenario token set (include 'after-colon' tokens)
    // -----------------------
    String scenUp = scenarioInfo.toUpperCase();

    // collect first token after each colon (if any)
    StringBuilder scenAfterColon = new StringBuilder();
    String[] scenColonSplit = scenUp.split(":");
    if (scenColonSplit.length > 1) {
        for (int i = 1; i < scenColonSplit.length; i++) {
            String part = scenColonSplit[i].trim();
            if (!part.isEmpty()) {
                String[] tok = part.split("\\s+");
                if (tok.length > 0 && !tok[0].isEmpty()) {
                    scenAfterColon.append(" ").append(tok[0]);
                }
            }
        }
    }

    String scenCombined = scenUp + " " + scenAfterColon.toString();
    // normalize: keep only A-Z0-9, treat others as separators
    String scenTokensRaw = scenCombined.replaceAll("[^A-Z0-9]", " ").trim();
    String[] scenParts = scenTokensRaw.isEmpty() ? new String[0] : scenTokensRaw.split("\\s+");
    java.util.Set<String> scenSet = new java.util.HashSet<>();
    for (String sp : scenParts) {
        if (sp != null && !sp.isEmpty()) scenSet.add(sp);
    }

    // -----------------------
    // Process POSSIBLE_VALIDATION tokens
    // -----------------------
    String[] validations = possibleVal.split(",");
    for (String v : validations) {
        if (v == null) continue;
        String orig = v.trim();
        if (orig.isEmpty()) continue;

        // If contains ':', use the part after first colon as the "consider" token
        String consider = orig;
        if (orig.contains(":")) {
            String[] parts = orig.split(":", 2);
            if (parts.length == 2 && parts[1] != null && !parts[1].trim().isEmpty()) {
                consider = parts[1].trim();
            } else {
                consider = parts[0].trim();
            }
        }

        // Only consider tokens that are ALL CAPS (user requirement)
        if (!consider.equals(consider.toUpperCase())) {
            // skip non-CAPS tokens
            continue;
        }

        // Prepare normalized candidate keys for matching
        java.util.List<String> candidates = new java.util.ArrayList<>();
        String candFull = consider.toUpperCase().replaceAll("[^A-Z0-9]", "");
        if (!candFull.isEmpty()) candidates.add(candFull);

        // If contains FAIL, also add substring up to FAIL (inclusive)
        String upperConsider = consider.toUpperCase();
        if (upperConsider.contains("FAIL")) {
            int idx = upperConsider.indexOf("FAIL") + 4;
            String upToFail = upperConsider.substring(0, Math.min(idx, upperConsider.length()));
            String candFail = upToFail.replaceAll("[^A-Z0-9]", "");
            if (!candFail.isEmpty() && !candidates.contains(candFail)) candidates.add(candFail);
        }

        // Add tokenized pieces (split by non-alnum)
        String[] pieces = consider.toUpperCase().replaceAll("[^A-Z0-9]", " ").trim().split("\\s+");
        for (String p : pieces) {
            if (p != null && !p.isEmpty() && !candidates.contains(p)) candidates.add(p);
        }

        // Check if any candidate is present in scenarioSet
        boolean found = false;
        for (String cand : candidates) {
            if (cand == null || cand.isEmpty()) continue;
            if (scenSet.contains(cand)) {
                found = true;
                break;
            }
        }

        // If none matched, include the CAPS status (one per line)
        if (!found) {
            sb.append(consider.toUpperCase()).append("\n");
        }
    }

    output = sb.toString().trim();
}
