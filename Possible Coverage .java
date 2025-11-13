// ---------------------------------------------------
// CONDITION 4:
// Take POSSIBLE_VALIDATION values (in capital) as reference,
// Check against SCENARIO_INFO, and include ONLY those
// validations which are present in POSSIBLE_VALIDATION
// but NOT mentioned anywhere in SCENARIO_INFO.
// ---------------------------------------------------

else {

    StringBuilder sb = new StringBuilder();

    // Split POSSIBLE_VALIDATION values
    String[] validations = possibleVal.split(",");

    // Upper-case scenarioInfo for safe comparison
    String scenarioUpper = scenarioInfo.toUpperCase();

    for (String v : validations) {
        if (v == null) continue;
        String val = v.trim().toUpperCase();

        // If NOT present in SCENARIO_INFO, include it
        if (!scenarioUpper.contains(val)) {
            sb.append(val).append("\n");
        }
    }

    output = sb.toString().trim();
}
