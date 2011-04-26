package org.apache.continuum.web.util;

public class RegexPatternConstants
{
    public static final String NAME_REGEX = "[a-zA-Z0-9\\s_.:-]*";

    public static final String GROUP_ID_REGEX = "[a-zA-Z0-9.\\s]*";
    
    public static final String VERSION_REGEX = "[a-zA-Z0-9.-]*";
    
    public static final String SCM_URL_REGEX = "[a-zA-Z0-9_.:${}#~=@\\\\/|\\[\\]-]*";
    
    public static final String DESCRIPTION_REGEX = "[a-zA-Z0-9\\s_.-]*";

}
