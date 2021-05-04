package core.parsers;

import javax.validation.constraints.NotNull;
import java.util.Collections;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OptionalEntity {
    private static final Pattern options = Pattern.compile("(?:--|~~|—)(\\w+)");
    private final String value;
    private final String definition;
    private final boolean isEnabledByDefault;
    private final Set<String> blockedBy;


    public OptionalEntity(String value, String definition) {
        this(value, definition, false, Collections.emptySet());
    }


    public OptionalEntity(String value, String definition, boolean isEnabledByDefault, String blockedBy) {
        this.value = value;
        this.definition = definition;
        this.isEnabledByDefault = isEnabledByDefault;
        this.blockedBy = Set.of(blockedBy);
    }

    public OptionalEntity(String value, String definition, boolean isEnabledByDefault, Set<String> blockedBy) {
        this.value = value;
        this.definition = definition;
        this.isEnabledByDefault = isEnabledByDefault;
        this.blockedBy = blockedBy;
    }

    public static boolean isWordAValidOptional(Set<OptionalEntity> optPool, String toTest) {
        Matcher matcher = options.matcher(toTest);
        return matcher.matches() && optPool.contains(new OptionalEntity(matcher.group(1), ""));
    }

    /**
     * @param valid Needs to be a previously validated with {@link #options}
     * @return the substring without the optional prefixes
     */
    //Valid needs to
    public static String getOptPartFromValid(@NotNull String valid) {

        Matcher matcher = options.matcher(valid);
        if (matcher.matches()) {
            return matcher.group(1);
        }
        throw new IllegalStateException();
    }

    public String getDefinition() {
        return "\t Can use **" + "--" + value + "** to " + definition + "\n";
    }

    public String getDescription() {
        return "\tCan be use to " + definition + "\n";
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }

        if (!(o instanceof OptionalEntity)) {
            return false;
        }
        return this.value.equalsIgnoreCase(((OptionalEntity) o).value);
    }

    public String getValue() {
        return value;
    }

    public boolean isEnabledByDefault() {
        return isEnabledByDefault;
    }

    public Set<String> getBlockedBy() {
        return blockedBy;
    }
}
