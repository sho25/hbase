begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|filter
package|;
end_package

begin_import
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|*
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|regex
operator|.
name|Pattern
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|filter
operator|.
name|RegexStringComparator
operator|.
name|EngineType
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|testclassification
operator|.
name|FilterTests
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|testclassification
operator|.
name|SmallTests
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|util
operator|.
name|Bytes
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Test
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|experimental
operator|.
name|categories
operator|.
name|Category
import|;
end_import

begin_class
annotation|@
name|Category
argument_list|(
block|{
name|FilterTests
operator|.
name|class
block|,
name|SmallTests
operator|.
name|class
block|}
argument_list|)
specifier|public
class|class
name|TestRegexComparator
block|{
annotation|@
name|Test
specifier|public
name|void
name|testSerialization
parameter_list|()
throws|throws
name|Exception
block|{
comment|// Default engine is the Java engine
name|RegexStringComparator
name|a
init|=
operator|new
name|RegexStringComparator
argument_list|(
literal|"a|b"
argument_list|)
decl_stmt|;
name|RegexStringComparator
name|b
init|=
name|RegexStringComparator
operator|.
name|parseFrom
argument_list|(
name|a
operator|.
name|toByteArray
argument_list|()
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|a
operator|.
name|areSerializedFieldsEqual
argument_list|(
name|b
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|b
operator|.
name|getEngine
argument_list|()
operator|instanceof
name|RegexStringComparator
operator|.
name|JavaRegexEngine
argument_list|)
expr_stmt|;
comment|// joni engine
name|a
operator|=
operator|new
name|RegexStringComparator
argument_list|(
literal|"a|b"
argument_list|,
name|EngineType
operator|.
name|JONI
argument_list|)
expr_stmt|;
name|b
operator|=
name|RegexStringComparator
operator|.
name|parseFrom
argument_list|(
name|a
operator|.
name|toByteArray
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|a
operator|.
name|areSerializedFieldsEqual
argument_list|(
name|b
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|b
operator|.
name|getEngine
argument_list|()
operator|instanceof
name|RegexStringComparator
operator|.
name|JoniRegexEngine
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testJavaEngine
parameter_list|()
throws|throws
name|Exception
block|{
for|for
control|(
name|TestCase
name|t
range|:
name|TEST_CASES
control|)
block|{
name|boolean
name|result
init|=
operator|new
name|RegexStringComparator
argument_list|(
name|t
operator|.
name|regex
argument_list|,
name|t
operator|.
name|flags
argument_list|,
name|EngineType
operator|.
name|JAVA
argument_list|)
operator|.
name|compareTo
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|t
operator|.
name|haystack
argument_list|)
argument_list|)
operator|==
literal|0
decl_stmt|;
name|assertEquals
argument_list|(
literal|"Regex '"
operator|+
name|t
operator|.
name|regex
operator|+
literal|"' failed test '"
operator|+
name|t
operator|.
name|haystack
operator|+
literal|"'"
argument_list|,
name|result
argument_list|,
name|t
operator|.
name|expected
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Test
specifier|public
name|void
name|testJoniEngine
parameter_list|()
throws|throws
name|Exception
block|{
for|for
control|(
name|TestCase
name|t
range|:
name|TEST_CASES
control|)
block|{
name|boolean
name|result
init|=
operator|new
name|RegexStringComparator
argument_list|(
name|t
operator|.
name|regex
argument_list|,
name|t
operator|.
name|flags
argument_list|,
name|EngineType
operator|.
name|JONI
argument_list|)
operator|.
name|compareTo
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|t
operator|.
name|haystack
argument_list|)
argument_list|)
operator|==
literal|0
decl_stmt|;
name|assertEquals
argument_list|(
literal|"Regex '"
operator|+
name|t
operator|.
name|regex
operator|+
literal|"' failed test '"
operator|+
name|t
operator|.
name|haystack
operator|+
literal|"'"
argument_list|,
name|result
argument_list|,
name|t
operator|.
name|expected
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
specifier|static
class|class
name|TestCase
block|{
name|String
name|regex
decl_stmt|;
name|String
name|haystack
decl_stmt|;
name|int
name|flags
decl_stmt|;
name|boolean
name|expected
decl_stmt|;
specifier|public
name|TestCase
parameter_list|(
name|String
name|regex
parameter_list|,
name|String
name|haystack
parameter_list|,
name|boolean
name|expected
parameter_list|)
block|{
name|this
argument_list|(
name|regex
argument_list|,
name|Pattern
operator|.
name|DOTALL
argument_list|,
name|haystack
argument_list|,
name|expected
argument_list|)
expr_stmt|;
block|}
specifier|public
name|TestCase
parameter_list|(
name|String
name|regex
parameter_list|,
name|int
name|flags
parameter_list|,
name|String
name|haystack
parameter_list|,
name|boolean
name|expected
parameter_list|)
block|{
name|this
operator|.
name|regex
operator|=
name|regex
expr_stmt|;
name|this
operator|.
name|flags
operator|=
name|flags
expr_stmt|;
name|this
operator|.
name|haystack
operator|=
name|haystack
expr_stmt|;
name|this
operator|.
name|expected
operator|=
name|expected
expr_stmt|;
block|}
block|}
comment|// These are a subset of the regex tests from OpenJDK 7
specifier|private
specifier|static
name|TestCase
name|TEST_CASES
index|[]
init|=
block|{
operator|new
name|TestCase
argument_list|(
literal|"a|b"
argument_list|,
literal|"a"
argument_list|,
literal|true
argument_list|)
block|,
operator|new
name|TestCase
argument_list|(
literal|"a|b"
argument_list|,
literal|"b"
argument_list|,
literal|true
argument_list|)
block|,
operator|new
name|TestCase
argument_list|(
literal|"a|b"
argument_list|,
name|Pattern
operator|.
name|CASE_INSENSITIVE
argument_list|,
literal|"A"
argument_list|,
literal|true
argument_list|)
block|,
operator|new
name|TestCase
argument_list|(
literal|"a|b"
argument_list|,
name|Pattern
operator|.
name|CASE_INSENSITIVE
argument_list|,
literal|"B"
argument_list|,
literal|true
argument_list|)
block|,
operator|new
name|TestCase
argument_list|(
literal|"a|b"
argument_list|,
literal|"z"
argument_list|,
literal|false
argument_list|)
block|,
operator|new
name|TestCase
argument_list|(
literal|"a|b|cd"
argument_list|,
literal|"cd"
argument_list|,
literal|true
argument_list|)
block|,
operator|new
name|TestCase
argument_list|(
literal|"z(a|ac)b"
argument_list|,
literal|"zacb"
argument_list|,
literal|true
argument_list|)
block|,
operator|new
name|TestCase
argument_list|(
literal|"[abc]+"
argument_list|,
literal|"ababab"
argument_list|,
literal|true
argument_list|)
block|,
operator|new
name|TestCase
argument_list|(
literal|"[abc]+"
argument_list|,
literal|"defg"
argument_list|,
literal|false
argument_list|)
block|,
operator|new
name|TestCase
argument_list|(
literal|"[abc]+[def]+[ghi]+"
argument_list|,
literal|"zzzaaddggzzz"
argument_list|,
literal|true
argument_list|)
block|,
operator|new
name|TestCase
argument_list|(
literal|"[a-\\u4444]+"
argument_list|,
literal|"za-9z"
argument_list|,
literal|true
argument_list|)
block|,
operator|new
name|TestCase
argument_list|(
literal|"[^abc]+"
argument_list|,
literal|"ababab"
argument_list|,
literal|false
argument_list|)
block|,
operator|new
name|TestCase
argument_list|(
literal|"[^abc]+"
argument_list|,
literal|"aaabbbcccdefg"
argument_list|,
literal|true
argument_list|)
block|,
operator|new
name|TestCase
argument_list|(
literal|"[abc^b]"
argument_list|,
literal|"b"
argument_list|,
literal|true
argument_list|)
block|,
operator|new
name|TestCase
argument_list|(
literal|"[abc[def]]"
argument_list|,
literal|"b"
argument_list|,
literal|true
argument_list|)
block|,
operator|new
name|TestCase
argument_list|(
literal|"[abc[def]]"
argument_list|,
literal|"e"
argument_list|,
literal|true
argument_list|)
block|,
operator|new
name|TestCase
argument_list|(
literal|"[a-c[d-f[g-i]]]"
argument_list|,
literal|"h"
argument_list|,
literal|true
argument_list|)
block|,
operator|new
name|TestCase
argument_list|(
literal|"[a-c[d-f[g-i]]m]"
argument_list|,
literal|"m"
argument_list|,
literal|true
argument_list|)
block|,
operator|new
name|TestCase
argument_list|(
literal|"[a-c&&[d-f]]"
argument_list|,
literal|"a"
argument_list|,
literal|false
argument_list|)
block|,
operator|new
name|TestCase
argument_list|(
literal|"[a-c&&[d-f]]"
argument_list|,
literal|"z"
argument_list|,
literal|false
argument_list|)
block|,
operator|new
name|TestCase
argument_list|(
literal|"[a-m&&m-z&&a-c]"
argument_list|,
literal|"m"
argument_list|,
literal|false
argument_list|)
block|,
operator|new
name|TestCase
argument_list|(
literal|"[a-m&&m-z&&a-z]"
argument_list|,
literal|"m"
argument_list|,
literal|true
argument_list|)
block|,
operator|new
name|TestCase
argument_list|(
literal|"[[a-m]&&[^a-c]]"
argument_list|,
literal|"a"
argument_list|,
literal|false
argument_list|)
block|,
operator|new
name|TestCase
argument_list|(
literal|"[[a-m]&&[^a-c]]"
argument_list|,
literal|"d"
argument_list|,
literal|true
argument_list|)
block|,
operator|new
name|TestCase
argument_list|(
literal|"[[a-c][d-f]&&abc[def]]"
argument_list|,
literal|"e"
argument_list|,
literal|true
argument_list|)
block|,
operator|new
name|TestCase
argument_list|(
literal|"[[a-c]&&[b-d]&&[c-e]]"
argument_list|,
literal|"c"
argument_list|,
literal|true
argument_list|)
block|,
operator|new
name|TestCase
argument_list|(
literal|"[[a-c]&&[b-d][c-e]&&[u-z]]"
argument_list|,
literal|"c"
argument_list|,
literal|false
argument_list|)
block|,
operator|new
name|TestCase
argument_list|(
literal|"[[a]&&[b][c][a]&&[^d]]"
argument_list|,
literal|"a"
argument_list|,
literal|true
argument_list|)
block|,
operator|new
name|TestCase
argument_list|(
literal|"[[a]&&[b][c][a]&&[^d]]"
argument_list|,
literal|"d"
argument_list|,
literal|false
argument_list|)
block|,
operator|new
name|TestCase
argument_list|(
literal|"[[[a-d]&&[c-f]]&&[c]&&c&&[cde]]"
argument_list|,
literal|"c"
argument_list|,
literal|true
argument_list|)
block|,
operator|new
name|TestCase
argument_list|(
literal|"[x[[wz]abc&&bcd[z]]&&[u-z]]"
argument_list|,
literal|"z"
argument_list|,
literal|true
argument_list|)
block|,
operator|new
name|TestCase
argument_list|(
literal|"a.c.+"
argument_list|,
literal|"a#c%&"
argument_list|,
literal|true
argument_list|)
block|,
operator|new
name|TestCase
argument_list|(
literal|"ab."
argument_list|,
literal|"ab\n"
argument_list|,
literal|true
argument_list|)
block|,
operator|new
name|TestCase
argument_list|(
literal|"(?s)ab."
argument_list|,
literal|"ab\n"
argument_list|,
literal|true
argument_list|)
block|,
operator|new
name|TestCase
argument_list|(
literal|"ab\\wc"
argument_list|,
literal|"abcc"
argument_list|,
literal|true
argument_list|)
block|,
operator|new
name|TestCase
argument_list|(
literal|"\\W\\w\\W"
argument_list|,
literal|"#r#"
argument_list|,
literal|true
argument_list|)
block|,
operator|new
name|TestCase
argument_list|(
literal|"\\W\\w\\W"
argument_list|,
literal|"rrrr#ggg"
argument_list|,
literal|false
argument_list|)
block|,
operator|new
name|TestCase
argument_list|(
literal|"abc[\\sdef]*"
argument_list|,
literal|"abc  def"
argument_list|,
literal|true
argument_list|)
block|,
operator|new
name|TestCase
argument_list|(
literal|"abc[\\sy-z]*"
argument_list|,
literal|"abc y z"
argument_list|,
literal|true
argument_list|)
block|,
operator|new
name|TestCase
argument_list|(
literal|"abc[a-d\\sm-p]*"
argument_list|,
literal|"abcaa mn  p"
argument_list|,
literal|true
argument_list|)
block|,
operator|new
name|TestCase
argument_list|(
literal|"\\s\\s\\s"
argument_list|,
literal|"blah  err"
argument_list|,
literal|false
argument_list|)
block|,
operator|new
name|TestCase
argument_list|(
literal|"\\S\\S\\s"
argument_list|,
literal|"blah  err"
argument_list|,
literal|true
argument_list|)
block|,
operator|new
name|TestCase
argument_list|(
literal|"ab\\dc"
argument_list|,
literal|"ab9c"
argument_list|,
literal|true
argument_list|)
block|,
operator|new
name|TestCase
argument_list|(
literal|"\\d\\d\\d"
argument_list|,
literal|"blah45"
argument_list|,
literal|false
argument_list|)
block|,
operator|new
name|TestCase
argument_list|(
literal|"^abc"
argument_list|,
literal|"abcdef"
argument_list|,
literal|true
argument_list|)
block|,
operator|new
name|TestCase
argument_list|(
literal|"^abc"
argument_list|,
literal|"bcdabc"
argument_list|,
literal|false
argument_list|)
block|,
operator|new
name|TestCase
argument_list|(
literal|"^(a)?a"
argument_list|,
literal|"a"
argument_list|,
literal|true
argument_list|)
block|,
operator|new
name|TestCase
argument_list|(
literal|"^(aa(bb)?)+$"
argument_list|,
literal|"aabbaa"
argument_list|,
literal|true
argument_list|)
block|,
operator|new
name|TestCase
argument_list|(
literal|"((a|b)?b)+"
argument_list|,
literal|"b"
argument_list|,
literal|true
argument_list|)
block|,
operator|new
name|TestCase
argument_list|(
literal|"^(a(b)?)+$"
argument_list|,
literal|"aba"
argument_list|,
literal|true
argument_list|)
block|,
operator|new
name|TestCase
argument_list|(
literal|"^(a(b(c)?)?)?abc"
argument_list|,
literal|"abc"
argument_list|,
literal|true
argument_list|)
block|,
operator|new
name|TestCase
argument_list|(
literal|"^(a(b(c))).*"
argument_list|,
literal|"abc"
argument_list|,
literal|true
argument_list|)
block|,
operator|new
name|TestCase
argument_list|(
literal|"a?b"
argument_list|,
literal|"aaaab"
argument_list|,
literal|true
argument_list|)
block|,
operator|new
name|TestCase
argument_list|(
literal|"a?b"
argument_list|,
literal|"aaacc"
argument_list|,
literal|false
argument_list|)
block|,
operator|new
name|TestCase
argument_list|(
literal|"a??b"
argument_list|,
literal|"aaaab"
argument_list|,
literal|true
argument_list|)
block|,
operator|new
name|TestCase
argument_list|(
literal|"a??b"
argument_list|,
literal|"aaacc"
argument_list|,
literal|false
argument_list|)
block|,
operator|new
name|TestCase
argument_list|(
literal|"a?+b"
argument_list|,
literal|"aaaab"
argument_list|,
literal|true
argument_list|)
block|,
operator|new
name|TestCase
argument_list|(
literal|"a?+b"
argument_list|,
literal|"aaacc"
argument_list|,
literal|false
argument_list|)
block|,
operator|new
name|TestCase
argument_list|(
literal|"a+b"
argument_list|,
literal|"aaaab"
argument_list|,
literal|true
argument_list|)
block|,
operator|new
name|TestCase
argument_list|(
literal|"a+b"
argument_list|,
literal|"aaacc"
argument_list|,
literal|false
argument_list|)
block|,
operator|new
name|TestCase
argument_list|(
literal|"a+?b"
argument_list|,
literal|"aaaab"
argument_list|,
literal|true
argument_list|)
block|,
operator|new
name|TestCase
argument_list|(
literal|"a+?b"
argument_list|,
literal|"aaacc"
argument_list|,
literal|false
argument_list|)
block|,
operator|new
name|TestCase
argument_list|(
literal|"a++b"
argument_list|,
literal|"aaaab"
argument_list|,
literal|true
argument_list|)
block|,
operator|new
name|TestCase
argument_list|(
literal|"a++b"
argument_list|,
literal|"aaacc"
argument_list|,
literal|false
argument_list|)
block|,
operator|new
name|TestCase
argument_list|(
literal|"a{2,3}"
argument_list|,
literal|"a"
argument_list|,
literal|false
argument_list|)
block|,
operator|new
name|TestCase
argument_list|(
literal|"a{2,3}"
argument_list|,
literal|"aa"
argument_list|,
literal|true
argument_list|)
block|,
operator|new
name|TestCase
argument_list|(
literal|"a{2,3}"
argument_list|,
literal|"aaa"
argument_list|,
literal|true
argument_list|)
block|,
operator|new
name|TestCase
argument_list|(
literal|"a{3,}"
argument_list|,
literal|"zzzaaaazzz"
argument_list|,
literal|true
argument_list|)
block|,
operator|new
name|TestCase
argument_list|(
literal|"a{3,}"
argument_list|,
literal|"zzzaazzz"
argument_list|,
literal|false
argument_list|)
block|,
operator|new
name|TestCase
argument_list|(
literal|"abc(?=d)"
argument_list|,
literal|"zzzabcd"
argument_list|,
literal|true
argument_list|)
block|,
operator|new
name|TestCase
argument_list|(
literal|"abc(?=d)"
argument_list|,
literal|"zzzabced"
argument_list|,
literal|false
argument_list|)
block|,
operator|new
name|TestCase
argument_list|(
literal|"abc(?!d)"
argument_list|,
literal|"zzabcd"
argument_list|,
literal|false
argument_list|)
block|,
operator|new
name|TestCase
argument_list|(
literal|"abc(?!d)"
argument_list|,
literal|"zzabced"
argument_list|,
literal|true
argument_list|)
block|,
operator|new
name|TestCase
argument_list|(
literal|"\\w(?<=a)"
argument_list|,
literal|"###abc###"
argument_list|,
literal|true
argument_list|)
block|,
operator|new
name|TestCase
argument_list|(
literal|"\\w(?<=a)"
argument_list|,
literal|"###ert###"
argument_list|,
literal|false
argument_list|)
block|,
operator|new
name|TestCase
argument_list|(
literal|"(?<!a)c"
argument_list|,
literal|"bc"
argument_list|,
literal|true
argument_list|)
block|,
operator|new
name|TestCase
argument_list|(
literal|"(?<!a)c"
argument_list|,
literal|"ac"
argument_list|,
literal|false
argument_list|)
block|,
operator|new
name|TestCase
argument_list|(
literal|"(a+b)+"
argument_list|,
literal|"ababab"
argument_list|,
literal|true
argument_list|)
block|,
operator|new
name|TestCase
argument_list|(
literal|"(a+b)+"
argument_list|,
literal|"accccd"
argument_list|,
literal|false
argument_list|)
block|,
operator|new
name|TestCase
argument_list|(
literal|"(ab)+"
argument_list|,
literal|"ababab"
argument_list|,
literal|true
argument_list|)
block|,
operator|new
name|TestCase
argument_list|(
literal|"(ab)+"
argument_list|,
literal|"accccd"
argument_list|,
literal|false
argument_list|)
block|,
operator|new
name|TestCase
argument_list|(
literal|"(ab)(cd*)"
argument_list|,
literal|"zzzabczzz"
argument_list|,
literal|true
argument_list|)
block|,
operator|new
name|TestCase
argument_list|(
literal|"abc(d)*abc"
argument_list|,
literal|"abcdddddabc"
argument_list|,
literal|true
argument_list|)
block|,
operator|new
name|TestCase
argument_list|(
literal|"a*b"
argument_list|,
literal|"aaaab"
argument_list|,
literal|true
argument_list|)
block|,
operator|new
name|TestCase
argument_list|(
literal|"a*b"
argument_list|,
literal|"b"
argument_list|,
literal|true
argument_list|)
block|,
operator|new
name|TestCase
argument_list|(
literal|"a*b"
argument_list|,
literal|"aaaac"
argument_list|,
literal|false
argument_list|)
block|,
operator|new
name|TestCase
argument_list|(
literal|".*?b"
argument_list|,
literal|"aaaab"
argument_list|,
literal|true
argument_list|)
block|,
operator|new
name|TestCase
argument_list|(
literal|"a*+b"
argument_list|,
literal|"aaaab"
argument_list|,
literal|true
argument_list|)
block|,
operator|new
name|TestCase
argument_list|(
literal|"a*+b"
argument_list|,
literal|"b"
argument_list|,
literal|true
argument_list|)
block|,
operator|new
name|TestCase
argument_list|(
literal|"a*+b"
argument_list|,
literal|"aaaac"
argument_list|,
literal|false
argument_list|)
block|,
operator|new
name|TestCase
argument_list|(
literal|"(?i)foobar"
argument_list|,
literal|"fOobAr"
argument_list|,
literal|true
argument_list|)
block|,
operator|new
name|TestCase
argument_list|(
literal|"f(?i)oobar"
argument_list|,
literal|"fOobAr"
argument_list|,
literal|true
argument_list|)
block|,
operator|new
name|TestCase
argument_list|(
literal|"f(?i)oobar"
argument_list|,
literal|"FOobAr"
argument_list|,
literal|false
argument_list|)
block|,
operator|new
name|TestCase
argument_list|(
literal|"foo(?i)bar"
argument_list|,
literal|"fOobAr"
argument_list|,
literal|false
argument_list|)
block|,
operator|new
name|TestCase
argument_list|(
literal|"(?i)foo[bar]+"
argument_list|,
literal|"foObAr"
argument_list|,
literal|true
argument_list|)
block|,
operator|new
name|TestCase
argument_list|(
literal|"(?i)foo[a-r]+"
argument_list|,
literal|"foObAr"
argument_list|,
literal|true
argument_list|)
block|,
operator|new
name|TestCase
argument_list|(
literal|"abc(?x)blah"
argument_list|,
literal|"abcblah"
argument_list|,
literal|true
argument_list|)
block|,
operator|new
name|TestCase
argument_list|(
literal|"abc(?x)  blah"
argument_list|,
literal|"abcblah"
argument_list|,
literal|true
argument_list|)
block|,
operator|new
name|TestCase
argument_list|(
literal|"abc(?x)  blah  blech"
argument_list|,
literal|"abcblahblech"
argument_list|,
literal|true
argument_list|)
block|,
operator|new
name|TestCase
argument_list|(
literal|"[\\n-#]"
argument_list|,
literal|"!"
argument_list|,
literal|true
argument_list|)
block|,
operator|new
name|TestCase
argument_list|(
literal|"[\\n-#]"
argument_list|,
literal|"-"
argument_list|,
literal|false
argument_list|)
block|,
operator|new
name|TestCase
argument_list|(
literal|"[\\043]+"
argument_list|,
literal|"blahblah#blech"
argument_list|,
literal|true
argument_list|)
block|,
operator|new
name|TestCase
argument_list|(
literal|"[\\042-\\044]+"
argument_list|,
literal|"blahblah#blech"
argument_list|,
literal|true
argument_list|)
block|,
operator|new
name|TestCase
argument_list|(
literal|"[\\u1234-\\u1236]"
argument_list|,
literal|"blahblah\u1235blech"
argument_list|,
literal|true
argument_list|)
block|,
operator|new
name|TestCase
argument_list|(
literal|"[^\043]*"
argument_list|,
literal|"blahblah#blech"
argument_list|,
literal|true
argument_list|)
block|,
operator|new
name|TestCase
argument_list|(
literal|"(|f)?+"
argument_list|,
literal|"foo"
argument_list|,
literal|true
argument_list|)
block|,   }
decl_stmt|;
block|}
end_class

end_unit

