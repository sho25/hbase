begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/* Generated By:JavaCC: Do not edit this line. ParserConstants.java */
end_comment

begin_package
package|package
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|shell
operator|.
name|generated
package|;
end_package

begin_interface
specifier|public
interface|interface
name|ParserConstants
block|{
name|int
name|EOF
init|=
literal|0
decl_stmt|;
name|int
name|HELP
init|=
literal|5
decl_stmt|;
name|int
name|ALTER
init|=
literal|6
decl_stmt|;
name|int
name|CLEAR
init|=
literal|7
decl_stmt|;
name|int
name|SHOW
init|=
literal|8
decl_stmt|;
name|int
name|DESCRIBE
init|=
literal|9
decl_stmt|;
name|int
name|DESC
init|=
literal|10
decl_stmt|;
name|int
name|CREATE
init|=
literal|11
decl_stmt|;
name|int
name|DROP
init|=
literal|12
decl_stmt|;
name|int
name|FS
init|=
literal|13
decl_stmt|;
name|int
name|JAR
init|=
literal|14
decl_stmt|;
name|int
name|EXIT
init|=
literal|15
decl_stmt|;
name|int
name|INSERT
init|=
literal|16
decl_stmt|;
name|int
name|INTO
init|=
literal|17
decl_stmt|;
name|int
name|TABLE
init|=
literal|18
decl_stmt|;
name|int
name|DELETE
init|=
literal|19
decl_stmt|;
name|int
name|SELECT
init|=
literal|20
decl_stmt|;
name|int
name|ENABLE
init|=
literal|21
decl_stmt|;
name|int
name|DISABLE
init|=
literal|22
decl_stmt|;
name|int
name|STARTING
init|=
literal|23
decl_stmt|;
name|int
name|WHERE
init|=
literal|24
decl_stmt|;
name|int
name|FROM
init|=
literal|25
decl_stmt|;
name|int
name|ROW
init|=
literal|26
decl_stmt|;
name|int
name|VALUES
init|=
literal|27
decl_stmt|;
name|int
name|COLUMNFAMILIES
init|=
literal|28
decl_stmt|;
name|int
name|TIMESTAMP
init|=
literal|29
decl_stmt|;
name|int
name|NUM_VERSIONS
init|=
literal|30
decl_stmt|;
name|int
name|LIMIT
init|=
literal|31
decl_stmt|;
name|int
name|AND
init|=
literal|32
decl_stmt|;
name|int
name|OR
init|=
literal|33
decl_stmt|;
name|int
name|COMMA
init|=
literal|34
decl_stmt|;
name|int
name|DOT
init|=
literal|35
decl_stmt|;
name|int
name|LPAREN
init|=
literal|36
decl_stmt|;
name|int
name|RPAREN
init|=
literal|37
decl_stmt|;
name|int
name|EQUALS
init|=
literal|38
decl_stmt|;
name|int
name|NOTEQUAL
init|=
literal|39
decl_stmt|;
name|int
name|ASTERISK
init|=
literal|40
decl_stmt|;
name|int
name|MAX_VERSIONS
init|=
literal|41
decl_stmt|;
name|int
name|MAX_LENGTH
init|=
literal|42
decl_stmt|;
name|int
name|COMPRESSION
init|=
literal|43
decl_stmt|;
name|int
name|NONE
init|=
literal|44
decl_stmt|;
name|int
name|BLOCK
init|=
literal|45
decl_stmt|;
name|int
name|RECORD
init|=
literal|46
decl_stmt|;
name|int
name|IN_MEMORY
init|=
literal|47
decl_stmt|;
name|int
name|BLOOMFILTER
init|=
literal|48
decl_stmt|;
name|int
name|COUNTING_BLOOMFILTER
init|=
literal|49
decl_stmt|;
name|int
name|RETOUCHED_BLOOMFILTER
init|=
literal|50
decl_stmt|;
name|int
name|VECTOR_SIZE
init|=
literal|51
decl_stmt|;
name|int
name|NUM_HASH
init|=
literal|52
decl_stmt|;
name|int
name|NUM_ENTRIES
init|=
literal|53
decl_stmt|;
name|int
name|ADD
init|=
literal|54
decl_stmt|;
name|int
name|CHANGE
init|=
literal|55
decl_stmt|;
name|int
name|ID
init|=
literal|56
decl_stmt|;
name|int
name|INTEGER_LITERAL
init|=
literal|57
decl_stmt|;
name|int
name|FLOATING_POINT_LITERAL
init|=
literal|58
decl_stmt|;
name|int
name|EXPONENT
init|=
literal|59
decl_stmt|;
name|int
name|QUOTED_IDENTIFIER
init|=
literal|60
decl_stmt|;
name|int
name|STRING_LITERAL
init|=
literal|61
decl_stmt|;
name|int
name|DEFAULT
init|=
literal|0
decl_stmt|;
name|String
index|[]
name|tokenImage
init|=
block|{
literal|"<EOF>"
block|,
literal|"\" \""
block|,
literal|"\"\\t\""
block|,
literal|"\"\\r\""
block|,
literal|"\"\\n\""
block|,
literal|"\"help\""
block|,
literal|"\"alter\""
block|,
literal|"\"clear\""
block|,
literal|"\"show\""
block|,
literal|"\"describe\""
block|,
literal|"\"desc\""
block|,
literal|"\"create\""
block|,
literal|"\"drop\""
block|,
literal|"\"fs\""
block|,
literal|"\"jar\""
block|,
literal|"\"exit\""
block|,
literal|"\"insert\""
block|,
literal|"\"into\""
block|,
literal|"\"table\""
block|,
literal|"\"delete\""
block|,
literal|"\"select\""
block|,
literal|"\"enable\""
block|,
literal|"\"disable\""
block|,
literal|"\"starting\""
block|,
literal|"\"where\""
block|,
literal|"\"from\""
block|,
literal|"\"row\""
block|,
literal|"\"values\""
block|,
literal|"\"columnfamilies\""
block|,
literal|"\"timestamp\""
block|,
literal|"\"num_versions\""
block|,
literal|"\"limit\""
block|,
literal|"\"and\""
block|,
literal|"\"or\""
block|,
literal|"\",\""
block|,
literal|"\".\""
block|,
literal|"\"(\""
block|,
literal|"\")\""
block|,
literal|"\"=\""
block|,
literal|"\"<>\""
block|,
literal|"\"*\""
block|,
literal|"\"max_versions\""
block|,
literal|"\"max_length\""
block|,
literal|"\"compression\""
block|,
literal|"\"none\""
block|,
literal|"\"block\""
block|,
literal|"\"record\""
block|,
literal|"\"in_memory\""
block|,
literal|"\"bloomfilter\""
block|,
literal|"\"counting_bloomfilter\""
block|,
literal|"\"retouched_bloomfilter\""
block|,
literal|"\"vector_size\""
block|,
literal|"\"num_hash\""
block|,
literal|"\"num_entries\""
block|,
literal|"\"add\""
block|,
literal|"\"change\""
block|,
literal|"<ID>"
block|,
literal|"<INTEGER_LITERAL>"
block|,
literal|"<FLOATING_POINT_LITERAL>"
block|,
literal|"<EXPONENT>"
block|,
literal|"<QUOTED_IDENTIFIER>"
block|,
literal|"<STRING_LITERAL>"
block|,
literal|"\";\""
block|,   }
decl_stmt|;
block|}
end_interface

end_unit

