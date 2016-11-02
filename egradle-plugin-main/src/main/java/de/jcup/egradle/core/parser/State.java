package de.jcup.egradle.core.parser;

enum State {
	BRACKET_END_FOUND,

	BRACKET_START_FOUND,

	INITIALIZING,

	MULTILINE_COMMENT_END_FOUND,

	MULTILINE_COMMENT_START_FOUND,

	SINGLE_LINE_COMMENT_START_FOUND,
	
	NEW_LINE_TEXT_PARSING,
	
	SINGLE_QUOTE_START,
	
	SINGLE_QUOTE_END,
	
	DOUBLE_QUOTE_START,
	
	DOUBLE_QUOTE_END,
	

}