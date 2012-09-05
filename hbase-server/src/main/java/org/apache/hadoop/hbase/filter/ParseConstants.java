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
import|import
name|java
operator|.
name|nio
operator|.
name|ByteBuffer
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
name|classification
operator|.
name|InterfaceAudience
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
name|classification
operator|.
name|InterfaceStability
import|;
end_import

begin_comment
comment|/**  * ParseConstants holds a bunch of constants related to parsing Filter Strings  * Used by {@link ParseFilter}  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Public
annotation|@
name|InterfaceStability
operator|.
name|Stable
specifier|public
specifier|final
class|class
name|ParseConstants
block|{
comment|/**    * ASCII code for LPAREN    */
specifier|public
specifier|static
specifier|final
name|int
name|LPAREN
init|=
literal|'('
decl_stmt|;
comment|/**    * ASCII code for RPAREN    */
specifier|public
specifier|static
specifier|final
name|int
name|RPAREN
init|=
literal|')'
decl_stmt|;
comment|/**    * ASCII code for whitespace    */
specifier|public
specifier|static
specifier|final
name|int
name|WHITESPACE
init|=
literal|' '
decl_stmt|;
comment|/**    * ASCII code for tab    */
specifier|public
specifier|static
specifier|final
name|int
name|TAB
init|=
literal|'\t'
decl_stmt|;
comment|/**    * ASCII code for 'A'    */
specifier|public
specifier|static
specifier|final
name|int
name|A
init|=
literal|'A'
decl_stmt|;
comment|/**    * ASCII code for 'N'    */
specifier|public
specifier|static
specifier|final
name|int
name|N
init|=
literal|'N'
decl_stmt|;
comment|/**    * ASCII code for 'D'    */
specifier|public
specifier|static
specifier|final
name|int
name|D
init|=
literal|'D'
decl_stmt|;
comment|/**    * ASCII code for 'O'    */
specifier|public
specifier|static
specifier|final
name|int
name|O
init|=
literal|'O'
decl_stmt|;
comment|/**    * ASCII code for 'R'    */
specifier|public
specifier|static
specifier|final
name|int
name|R
init|=
literal|'R'
decl_stmt|;
comment|/**    * ASCII code for 'S'    */
specifier|public
specifier|static
specifier|final
name|int
name|S
init|=
literal|'S'
decl_stmt|;
comment|/**    * ASCII code for 'K'    */
specifier|public
specifier|static
specifier|final
name|int
name|K
init|=
literal|'K'
decl_stmt|;
comment|/**    * ASCII code for 'I'    */
specifier|public
specifier|static
specifier|final
name|int
name|I
init|=
literal|'I'
decl_stmt|;
comment|/**    * ASCII code for 'P'    */
specifier|public
specifier|static
specifier|final
name|int
name|P
init|=
literal|'P'
decl_stmt|;
comment|/**    * SKIP Array    */
specifier|public
specifier|static
specifier|final
name|byte
index|[]
name|SKIP_ARRAY
init|=
operator|new
name|byte
index|[ ]
block|{
literal|'S'
block|,
literal|'K'
block|,
literal|'I'
block|,
literal|'P'
block|}
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|ByteBuffer
name|SKIP_BUFFER
init|=
name|ByteBuffer
operator|.
name|wrap
argument_list|(
name|SKIP_ARRAY
argument_list|)
decl_stmt|;
comment|/**    * ASCII code for 'W'    */
specifier|public
specifier|static
specifier|final
name|int
name|W
init|=
literal|'W'
decl_stmt|;
comment|/**    * ASCII code for 'H'    */
specifier|public
specifier|static
specifier|final
name|int
name|H
init|=
literal|'H'
decl_stmt|;
comment|/**    * ASCII code for 'L'    */
specifier|public
specifier|static
specifier|final
name|int
name|L
init|=
literal|'L'
decl_stmt|;
comment|/**    * ASCII code for 'E'    */
specifier|public
specifier|static
specifier|final
name|int
name|E
init|=
literal|'E'
decl_stmt|;
comment|/**    * WHILE Array    */
specifier|public
specifier|static
specifier|final
name|byte
index|[]
name|WHILE_ARRAY
init|=
operator|new
name|byte
index|[]
block|{
literal|'W'
block|,
literal|'H'
block|,
literal|'I'
block|,
literal|'L'
block|,
literal|'E'
block|}
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|ByteBuffer
name|WHILE_BUFFER
init|=
name|ByteBuffer
operator|.
name|wrap
argument_list|(
name|WHILE_ARRAY
argument_list|)
decl_stmt|;
comment|/**    * OR Array    */
specifier|public
specifier|static
specifier|final
name|byte
index|[]
name|OR_ARRAY
init|=
operator|new
name|byte
index|[]
block|{
literal|'O'
block|,
literal|'R'
block|}
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|ByteBuffer
name|OR_BUFFER
init|=
name|ByteBuffer
operator|.
name|wrap
argument_list|(
name|OR_ARRAY
argument_list|)
decl_stmt|;
comment|/**    * AND Array    */
specifier|public
specifier|static
specifier|final
name|byte
index|[]
name|AND_ARRAY
init|=
operator|new
name|byte
index|[]
block|{
literal|'A'
block|,
literal|'N'
block|,
literal|'D'
block|}
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|ByteBuffer
name|AND_BUFFER
init|=
name|ByteBuffer
operator|.
name|wrap
argument_list|(
name|AND_ARRAY
argument_list|)
decl_stmt|;
comment|/**    * ASCII code for Backslash    */
specifier|public
specifier|static
specifier|final
name|int
name|BACKSLASH
init|=
literal|'\\'
decl_stmt|;
comment|/**    * ASCII code for a single quote    */
specifier|public
specifier|static
specifier|final
name|int
name|SINGLE_QUOTE
init|=
literal|'\''
decl_stmt|;
comment|/**    * ASCII code for a comma    */
specifier|public
specifier|static
specifier|final
name|int
name|COMMA
init|=
literal|','
decl_stmt|;
comment|/**    * LESS_THAN Array    */
specifier|public
specifier|static
specifier|final
name|byte
index|[]
name|LESS_THAN_ARRAY
init|=
operator|new
name|byte
index|[]
block|{
literal|'<'
block|}
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|ByteBuffer
name|LESS_THAN_BUFFER
init|=
name|ByteBuffer
operator|.
name|wrap
argument_list|(
name|LESS_THAN_ARRAY
argument_list|)
decl_stmt|;
comment|/**    * LESS_THAN_OR_EQUAL_TO Array    */
specifier|public
specifier|static
specifier|final
name|byte
index|[]
name|LESS_THAN_OR_EQUAL_TO_ARRAY
init|=
operator|new
name|byte
index|[]
block|{
literal|'<'
block|,
literal|'='
block|}
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|ByteBuffer
name|LESS_THAN_OR_EQUAL_TO_BUFFER
init|=
name|ByteBuffer
operator|.
name|wrap
argument_list|(
name|LESS_THAN_OR_EQUAL_TO_ARRAY
argument_list|)
decl_stmt|;
comment|/**    * GREATER_THAN Array    */
specifier|public
specifier|static
specifier|final
name|byte
index|[]
name|GREATER_THAN_ARRAY
init|=
operator|new
name|byte
index|[]
block|{
literal|'>'
block|}
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|ByteBuffer
name|GREATER_THAN_BUFFER
init|=
name|ByteBuffer
operator|.
name|wrap
argument_list|(
name|GREATER_THAN_ARRAY
argument_list|)
decl_stmt|;
comment|/**    * GREATER_THAN_OR_EQUAL_TO Array    */
specifier|public
specifier|static
specifier|final
name|byte
index|[]
name|GREATER_THAN_OR_EQUAL_TO_ARRAY
init|=
operator|new
name|byte
index|[]
block|{
literal|'>'
block|,
literal|'='
block|}
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|ByteBuffer
name|GREATER_THAN_OR_EQUAL_TO_BUFFER
init|=
name|ByteBuffer
operator|.
name|wrap
argument_list|(
name|GREATER_THAN_OR_EQUAL_TO_ARRAY
argument_list|)
decl_stmt|;
comment|/**    * EQUAL_TO Array    */
specifier|public
specifier|static
specifier|final
name|byte
index|[]
name|EQUAL_TO_ARRAY
init|=
operator|new
name|byte
index|[]
block|{
literal|'='
block|}
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|ByteBuffer
name|EQUAL_TO_BUFFER
init|=
name|ByteBuffer
operator|.
name|wrap
argument_list|(
name|EQUAL_TO_ARRAY
argument_list|)
decl_stmt|;
comment|/**    * NOT_EQUAL_TO Array    */
specifier|public
specifier|static
specifier|final
name|byte
index|[]
name|NOT_EQUAL_TO_ARRAY
init|=
operator|new
name|byte
index|[]
block|{
literal|'!'
block|,
literal|'='
block|}
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|ByteBuffer
name|NOT_EQUAL_TO_BUFFER
init|=
name|ByteBuffer
operator|.
name|wrap
argument_list|(
name|NOT_EQUAL_TO_ARRAY
argument_list|)
decl_stmt|;
comment|/**    * ASCII code for equal to (=)    */
specifier|public
specifier|static
specifier|final
name|int
name|EQUAL_TO
init|=
literal|'='
decl_stmt|;
comment|/**    * AND Byte Array    */
specifier|public
specifier|static
specifier|final
name|byte
index|[]
name|AND
init|=
operator|new
name|byte
index|[]
block|{
literal|'A'
block|,
literal|'N'
block|,
literal|'D'
block|}
decl_stmt|;
comment|/**    * OR Byte Array    */
specifier|public
specifier|static
specifier|final
name|byte
index|[]
name|OR
init|=
operator|new
name|byte
index|[]
block|{
literal|'O'
block|,
literal|'R'
block|}
decl_stmt|;
comment|/**    * LPAREN Array    */
specifier|public
specifier|static
specifier|final
name|byte
index|[]
name|LPAREN_ARRAY
init|=
operator|new
name|byte
index|[]
block|{
literal|'('
block|}
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|ByteBuffer
name|LPAREN_BUFFER
init|=
name|ByteBuffer
operator|.
name|wrap
argument_list|(
name|LPAREN_ARRAY
argument_list|)
decl_stmt|;
comment|/**    * ASCII code for colon (:)    */
specifier|public
specifier|static
specifier|final
name|int
name|COLON
init|=
literal|':'
decl_stmt|;
comment|/**    * ASCII code for Zero    */
specifier|public
specifier|static
specifier|final
name|int
name|ZERO
init|=
literal|'0'
decl_stmt|;
comment|/**    * ASCII code foe Nine    */
specifier|public
specifier|static
specifier|final
name|int
name|NINE
init|=
literal|'9'
decl_stmt|;
comment|/**    * BinaryType byte array    */
specifier|public
specifier|static
specifier|final
name|byte
index|[]
name|binaryType
init|=
operator|new
name|byte
index|[]
block|{
literal|'b'
block|,
literal|'i'
block|,
literal|'n'
block|,
literal|'a'
block|,
literal|'r'
block|,
literal|'y'
block|}
decl_stmt|;
comment|/**    * BinaryPrefixType byte array    */
specifier|public
specifier|static
specifier|final
name|byte
index|[]
name|binaryPrefixType
init|=
operator|new
name|byte
index|[]
block|{
literal|'b'
block|,
literal|'i'
block|,
literal|'n'
block|,
literal|'a'
block|,
literal|'r'
block|,
literal|'y'
block|,
literal|'p'
block|,
literal|'r'
block|,
literal|'e'
block|,
literal|'f'
block|,
literal|'i'
block|,
literal|'x'
block|}
decl_stmt|;
comment|/**    * RegexStringType byte array    */
specifier|public
specifier|static
specifier|final
name|byte
index|[]
name|regexStringType
init|=
operator|new
name|byte
index|[]
block|{
literal|'r'
block|,
literal|'e'
block|,
literal|'g'
block|,
literal|'e'
block|,
literal|'x'
block|,
literal|'s'
block|,
literal|'t'
block|,
literal|'r'
block|,
literal|'i'
block|,
literal|'n'
block|,
literal|'g'
block|}
decl_stmt|;
comment|/**    * SubstringType byte array    */
specifier|public
specifier|static
specifier|final
name|byte
index|[]
name|substringType
init|=
operator|new
name|byte
index|[]
block|{
literal|'s'
block|,
literal|'u'
block|,
literal|'b'
block|,
literal|'s'
block|,
literal|'t'
block|,
literal|'r'
block|,
literal|'i'
block|,
literal|'n'
block|,
literal|'g'
block|}
decl_stmt|;
comment|/**    * ASCII for Minus Sign    */
specifier|public
specifier|static
specifier|final
name|int
name|MINUS_SIGN
init|=
literal|'-'
decl_stmt|;
comment|/**    * Package containing filters    */
specifier|public
specifier|static
specifier|final
name|String
name|FILTER_PACKAGE
init|=
literal|"org.apache.hadoop.hbase.filter"
decl_stmt|;
block|}
end_class

end_unit

