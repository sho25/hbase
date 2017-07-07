begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|mapreduce
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
name|assertEquals
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|assertFalse
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|assertNull
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|assertTrue
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|fail
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|ArrayList
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
name|HConstants
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
name|MapReduceTests
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
name|mapreduce
operator|.
name|ImportTsv
operator|.
name|TsvParser
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
name|mapreduce
operator|.
name|ImportTsv
operator|.
name|TsvParser
operator|.
name|BadTsvLineException
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
name|mapreduce
operator|.
name|ImportTsv
operator|.
name|TsvParser
operator|.
name|ParsedLine
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
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|util
operator|.
name|Pair
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
name|shaded
operator|.
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|base
operator|.
name|Joiner
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
name|shaded
operator|.
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|base
operator|.
name|Splitter
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
name|shaded
operator|.
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|Iterables
import|;
end_import

begin_comment
comment|/**  * Tests for {@link TsvParser}.  */
end_comment

begin_class
annotation|@
name|Category
argument_list|(
block|{
name|MapReduceTests
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
name|TestImportTsvParser
block|{
specifier|private
name|void
name|assertBytesEquals
parameter_list|(
name|byte
index|[]
name|a
parameter_list|,
name|byte
index|[]
name|b
parameter_list|)
block|{
name|assertEquals
argument_list|(
name|Bytes
operator|.
name|toStringBinary
argument_list|(
name|a
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toStringBinary
argument_list|(
name|b
argument_list|)
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|checkParsing
parameter_list|(
name|ParsedLine
name|parsed
parameter_list|,
name|Iterable
argument_list|<
name|String
argument_list|>
name|expected
parameter_list|)
block|{
name|ArrayList
argument_list|<
name|String
argument_list|>
name|parsedCols
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|parsed
operator|.
name|getColumnCount
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|parsedCols
operator|.
name|add
argument_list|(
name|Bytes
operator|.
name|toString
argument_list|(
name|parsed
operator|.
name|getLineBytes
argument_list|()
argument_list|,
name|parsed
operator|.
name|getColumnOffset
argument_list|(
name|i
argument_list|)
argument_list|,
name|parsed
operator|.
name|getColumnLength
argument_list|(
name|i
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
operator|!
name|Iterables
operator|.
name|elementsEqual
argument_list|(
name|parsedCols
argument_list|,
name|expected
argument_list|)
condition|)
block|{
name|fail
argument_list|(
literal|"Expected: "
operator|+
name|Joiner
operator|.
name|on
argument_list|(
literal|","
argument_list|)
operator|.
name|join
argument_list|(
name|expected
argument_list|)
operator|+
literal|"\n"
operator|+
literal|"Got:"
operator|+
name|Joiner
operator|.
name|on
argument_list|(
literal|","
argument_list|)
operator|.
name|join
argument_list|(
name|parsedCols
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Test
specifier|public
name|void
name|testTsvParserSpecParsing
parameter_list|()
block|{
name|TsvParser
name|parser
decl_stmt|;
name|parser
operator|=
operator|new
name|TsvParser
argument_list|(
literal|"HBASE_ROW_KEY"
argument_list|,
literal|"\t"
argument_list|)
expr_stmt|;
name|assertNull
argument_list|(
name|parser
operator|.
name|getFamily
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|assertNull
argument_list|(
name|parser
operator|.
name|getQualifier
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|parser
operator|.
name|getRowKeyColumnIndex
argument_list|()
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|parser
operator|.
name|hasTimestamp
argument_list|()
argument_list|)
expr_stmt|;
name|parser
operator|=
operator|new
name|TsvParser
argument_list|(
literal|"HBASE_ROW_KEY,col1:scol1"
argument_list|,
literal|"\t"
argument_list|)
expr_stmt|;
name|assertNull
argument_list|(
name|parser
operator|.
name|getFamily
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|assertNull
argument_list|(
name|parser
operator|.
name|getQualifier
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|assertBytesEquals
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"col1"
argument_list|)
argument_list|,
name|parser
operator|.
name|getFamily
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|assertBytesEquals
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"scol1"
argument_list|)
argument_list|,
name|parser
operator|.
name|getQualifier
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|parser
operator|.
name|getRowKeyColumnIndex
argument_list|()
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|parser
operator|.
name|hasTimestamp
argument_list|()
argument_list|)
expr_stmt|;
name|parser
operator|=
operator|new
name|TsvParser
argument_list|(
literal|"HBASE_ROW_KEY,col1:scol1,col1:scol2"
argument_list|,
literal|"\t"
argument_list|)
expr_stmt|;
name|assertNull
argument_list|(
name|parser
operator|.
name|getFamily
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|assertNull
argument_list|(
name|parser
operator|.
name|getQualifier
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|assertBytesEquals
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"col1"
argument_list|)
argument_list|,
name|parser
operator|.
name|getFamily
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|assertBytesEquals
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"scol1"
argument_list|)
argument_list|,
name|parser
operator|.
name|getQualifier
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|assertBytesEquals
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"col1"
argument_list|)
argument_list|,
name|parser
operator|.
name|getFamily
argument_list|(
literal|2
argument_list|)
argument_list|)
expr_stmt|;
name|assertBytesEquals
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"scol2"
argument_list|)
argument_list|,
name|parser
operator|.
name|getQualifier
argument_list|(
literal|2
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|parser
operator|.
name|getRowKeyColumnIndex
argument_list|()
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|parser
operator|.
name|hasTimestamp
argument_list|()
argument_list|)
expr_stmt|;
name|parser
operator|=
operator|new
name|TsvParser
argument_list|(
literal|"HBASE_ROW_KEY,col1:scol1,HBASE_TS_KEY,col1:scol2"
argument_list|,
literal|"\t"
argument_list|)
expr_stmt|;
name|assertNull
argument_list|(
name|parser
operator|.
name|getFamily
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|assertNull
argument_list|(
name|parser
operator|.
name|getQualifier
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|assertBytesEquals
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"col1"
argument_list|)
argument_list|,
name|parser
operator|.
name|getFamily
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|assertBytesEquals
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"scol1"
argument_list|)
argument_list|,
name|parser
operator|.
name|getQualifier
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|assertBytesEquals
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"col1"
argument_list|)
argument_list|,
name|parser
operator|.
name|getFamily
argument_list|(
literal|3
argument_list|)
argument_list|)
expr_stmt|;
name|assertBytesEquals
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"scol2"
argument_list|)
argument_list|,
name|parser
operator|.
name|getQualifier
argument_list|(
literal|3
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|parser
operator|.
name|getRowKeyColumnIndex
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|parser
operator|.
name|hasTimestamp
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|2
argument_list|,
name|parser
operator|.
name|getTimestampKeyColumnIndex
argument_list|()
argument_list|)
expr_stmt|;
name|parser
operator|=
operator|new
name|TsvParser
argument_list|(
literal|"HBASE_ROW_KEY,col1:scol1,HBASE_TS_KEY,col1:scol2,HBASE_ATTRIBUTES_KEY"
argument_list|,
literal|"\t"
argument_list|)
expr_stmt|;
name|assertNull
argument_list|(
name|parser
operator|.
name|getFamily
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|assertNull
argument_list|(
name|parser
operator|.
name|getQualifier
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|assertBytesEquals
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"col1"
argument_list|)
argument_list|,
name|parser
operator|.
name|getFamily
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|assertBytesEquals
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"scol1"
argument_list|)
argument_list|,
name|parser
operator|.
name|getQualifier
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|assertBytesEquals
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"col1"
argument_list|)
argument_list|,
name|parser
operator|.
name|getFamily
argument_list|(
literal|3
argument_list|)
argument_list|)
expr_stmt|;
name|assertBytesEquals
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"scol2"
argument_list|)
argument_list|,
name|parser
operator|.
name|getQualifier
argument_list|(
literal|3
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|parser
operator|.
name|getRowKeyColumnIndex
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|parser
operator|.
name|hasTimestamp
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|2
argument_list|,
name|parser
operator|.
name|getTimestampKeyColumnIndex
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|4
argument_list|,
name|parser
operator|.
name|getAttributesKeyColumnIndex
argument_list|()
argument_list|)
expr_stmt|;
name|parser
operator|=
operator|new
name|TsvParser
argument_list|(
literal|"HBASE_ATTRIBUTES_KEY,col1:scol1,HBASE_TS_KEY,col1:scol2,HBASE_ROW_KEY"
argument_list|,
literal|"\t"
argument_list|)
expr_stmt|;
name|assertNull
argument_list|(
name|parser
operator|.
name|getFamily
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|assertNull
argument_list|(
name|parser
operator|.
name|getQualifier
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|assertBytesEquals
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"col1"
argument_list|)
argument_list|,
name|parser
operator|.
name|getFamily
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|assertBytesEquals
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"scol1"
argument_list|)
argument_list|,
name|parser
operator|.
name|getQualifier
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|assertBytesEquals
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"col1"
argument_list|)
argument_list|,
name|parser
operator|.
name|getFamily
argument_list|(
literal|3
argument_list|)
argument_list|)
expr_stmt|;
name|assertBytesEquals
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"scol2"
argument_list|)
argument_list|,
name|parser
operator|.
name|getQualifier
argument_list|(
literal|3
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|4
argument_list|,
name|parser
operator|.
name|getRowKeyColumnIndex
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|parser
operator|.
name|hasTimestamp
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|2
argument_list|,
name|parser
operator|.
name|getTimestampKeyColumnIndex
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|parser
operator|.
name|getAttributesKeyColumnIndex
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testTsvParser
parameter_list|()
throws|throws
name|BadTsvLineException
block|{
name|TsvParser
name|parser
init|=
operator|new
name|TsvParser
argument_list|(
literal|"col_a,col_b:qual,HBASE_ROW_KEY,col_d"
argument_list|,
literal|"\t"
argument_list|)
decl_stmt|;
name|assertBytesEquals
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"col_a"
argument_list|)
argument_list|,
name|parser
operator|.
name|getFamily
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|assertBytesEquals
argument_list|(
name|HConstants
operator|.
name|EMPTY_BYTE_ARRAY
argument_list|,
name|parser
operator|.
name|getQualifier
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|assertBytesEquals
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"col_b"
argument_list|)
argument_list|,
name|parser
operator|.
name|getFamily
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|assertBytesEquals
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"qual"
argument_list|)
argument_list|,
name|parser
operator|.
name|getQualifier
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|assertNull
argument_list|(
name|parser
operator|.
name|getFamily
argument_list|(
literal|2
argument_list|)
argument_list|)
expr_stmt|;
name|assertNull
argument_list|(
name|parser
operator|.
name|getQualifier
argument_list|(
literal|2
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|2
argument_list|,
name|parser
operator|.
name|getRowKeyColumnIndex
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|TsvParser
operator|.
name|DEFAULT_TIMESTAMP_COLUMN_INDEX
argument_list|,
name|parser
operator|.
name|getTimestampKeyColumnIndex
argument_list|()
argument_list|)
expr_stmt|;
name|byte
index|[]
name|line
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"val_a\tval_b\tval_c\tval_d"
argument_list|)
decl_stmt|;
name|ParsedLine
name|parsed
init|=
name|parser
operator|.
name|parse
argument_list|(
name|line
argument_list|,
name|line
operator|.
name|length
argument_list|)
decl_stmt|;
name|checkParsing
argument_list|(
name|parsed
argument_list|,
name|Splitter
operator|.
name|on
argument_list|(
literal|"\t"
argument_list|)
operator|.
name|split
argument_list|(
name|Bytes
operator|.
name|toString
argument_list|(
name|line
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testTsvParserWithTimestamp
parameter_list|()
throws|throws
name|BadTsvLineException
block|{
name|TsvParser
name|parser
init|=
operator|new
name|TsvParser
argument_list|(
literal|"HBASE_ROW_KEY,HBASE_TS_KEY,col_a,"
argument_list|,
literal|"\t"
argument_list|)
decl_stmt|;
name|assertNull
argument_list|(
name|parser
operator|.
name|getFamily
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|assertNull
argument_list|(
name|parser
operator|.
name|getQualifier
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|assertNull
argument_list|(
name|parser
operator|.
name|getFamily
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|assertNull
argument_list|(
name|parser
operator|.
name|getQualifier
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|assertBytesEquals
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"col_a"
argument_list|)
argument_list|,
name|parser
operator|.
name|getFamily
argument_list|(
literal|2
argument_list|)
argument_list|)
expr_stmt|;
name|assertBytesEquals
argument_list|(
name|HConstants
operator|.
name|EMPTY_BYTE_ARRAY
argument_list|,
name|parser
operator|.
name|getQualifier
argument_list|(
literal|2
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|parser
operator|.
name|getRowKeyColumnIndex
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|parser
operator|.
name|getTimestampKeyColumnIndex
argument_list|()
argument_list|)
expr_stmt|;
name|byte
index|[]
name|line
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"rowkey\t1234\tval_a"
argument_list|)
decl_stmt|;
name|ParsedLine
name|parsed
init|=
name|parser
operator|.
name|parse
argument_list|(
name|line
argument_list|,
name|line
operator|.
name|length
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|1234l
argument_list|,
name|parsed
operator|.
name|getTimestamp
argument_list|(
operator|-
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|checkParsing
argument_list|(
name|parsed
argument_list|,
name|Splitter
operator|.
name|on
argument_list|(
literal|"\t"
argument_list|)
operator|.
name|split
argument_list|(
name|Bytes
operator|.
name|toString
argument_list|(
name|line
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|/**    * Test cases that throw BadTsvLineException    */
annotation|@
name|Test
argument_list|(
name|expected
operator|=
name|BadTsvLineException
operator|.
name|class
argument_list|)
specifier|public
name|void
name|testTsvParserBadTsvLineExcessiveColumns
parameter_list|()
throws|throws
name|BadTsvLineException
block|{
name|TsvParser
name|parser
init|=
operator|new
name|TsvParser
argument_list|(
literal|"HBASE_ROW_KEY,col_a"
argument_list|,
literal|"\t"
argument_list|)
decl_stmt|;
name|byte
index|[]
name|line
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"val_a\tval_b\tval_c"
argument_list|)
decl_stmt|;
name|parser
operator|.
name|parse
argument_list|(
name|line
argument_list|,
name|line
operator|.
name|length
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
argument_list|(
name|expected
operator|=
name|BadTsvLineException
operator|.
name|class
argument_list|)
specifier|public
name|void
name|testTsvParserBadTsvLineZeroColumn
parameter_list|()
throws|throws
name|BadTsvLineException
block|{
name|TsvParser
name|parser
init|=
operator|new
name|TsvParser
argument_list|(
literal|"HBASE_ROW_KEY,col_a"
argument_list|,
literal|"\t"
argument_list|)
decl_stmt|;
name|byte
index|[]
name|line
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|""
argument_list|)
decl_stmt|;
name|parser
operator|.
name|parse
argument_list|(
name|line
argument_list|,
name|line
operator|.
name|length
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
argument_list|(
name|expected
operator|=
name|BadTsvLineException
operator|.
name|class
argument_list|)
specifier|public
name|void
name|testTsvParserBadTsvLineOnlyKey
parameter_list|()
throws|throws
name|BadTsvLineException
block|{
name|TsvParser
name|parser
init|=
operator|new
name|TsvParser
argument_list|(
literal|"HBASE_ROW_KEY,col_a"
argument_list|,
literal|"\t"
argument_list|)
decl_stmt|;
name|byte
index|[]
name|line
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"key_only"
argument_list|)
decl_stmt|;
name|parser
operator|.
name|parse
argument_list|(
name|line
argument_list|,
name|line
operator|.
name|length
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
argument_list|(
name|expected
operator|=
name|BadTsvLineException
operator|.
name|class
argument_list|)
specifier|public
name|void
name|testTsvParserBadTsvLineNoRowKey
parameter_list|()
throws|throws
name|BadTsvLineException
block|{
name|TsvParser
name|parser
init|=
operator|new
name|TsvParser
argument_list|(
literal|"col_a,HBASE_ROW_KEY"
argument_list|,
literal|"\t"
argument_list|)
decl_stmt|;
name|byte
index|[]
name|line
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"only_cola_data_and_no_row_key"
argument_list|)
decl_stmt|;
name|parser
operator|.
name|parse
argument_list|(
name|line
argument_list|,
name|line
operator|.
name|length
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
argument_list|(
name|expected
operator|=
name|BadTsvLineException
operator|.
name|class
argument_list|)
specifier|public
name|void
name|testTsvParserInvalidTimestamp
parameter_list|()
throws|throws
name|BadTsvLineException
block|{
name|TsvParser
name|parser
init|=
operator|new
name|TsvParser
argument_list|(
literal|"HBASE_ROW_KEY,HBASE_TS_KEY,col_a,"
argument_list|,
literal|"\t"
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|parser
operator|.
name|getTimestampKeyColumnIndex
argument_list|()
argument_list|)
expr_stmt|;
name|byte
index|[]
name|line
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"rowkey\ttimestamp\tval_a"
argument_list|)
decl_stmt|;
name|ParsedLine
name|parsed
init|=
name|parser
operator|.
name|parse
argument_list|(
name|line
argument_list|,
name|line
operator|.
name|length
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
operator|-
literal|1
argument_list|,
name|parsed
operator|.
name|getTimestamp
argument_list|(
operator|-
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|checkParsing
argument_list|(
name|parsed
argument_list|,
name|Splitter
operator|.
name|on
argument_list|(
literal|"\t"
argument_list|)
operator|.
name|split
argument_list|(
name|Bytes
operator|.
name|toString
argument_list|(
name|line
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
argument_list|(
name|expected
operator|=
name|BadTsvLineException
operator|.
name|class
argument_list|)
specifier|public
name|void
name|testTsvParserNoTimestampValue
parameter_list|()
throws|throws
name|BadTsvLineException
block|{
name|TsvParser
name|parser
init|=
operator|new
name|TsvParser
argument_list|(
literal|"HBASE_ROW_KEY,col_a,HBASE_TS_KEY"
argument_list|,
literal|"\t"
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|2
argument_list|,
name|parser
operator|.
name|getTimestampKeyColumnIndex
argument_list|()
argument_list|)
expr_stmt|;
name|byte
index|[]
name|line
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"rowkey\tval_a"
argument_list|)
decl_stmt|;
name|parser
operator|.
name|parse
argument_list|(
name|line
argument_list|,
name|line
operator|.
name|length
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testTsvParserParseRowKey
parameter_list|()
throws|throws
name|BadTsvLineException
block|{
name|TsvParser
name|parser
init|=
operator|new
name|TsvParser
argument_list|(
literal|"HBASE_ROW_KEY,col_a,HBASE_TS_KEY"
argument_list|,
literal|"\t"
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|parser
operator|.
name|getRowKeyColumnIndex
argument_list|()
argument_list|)
expr_stmt|;
name|byte
index|[]
name|line
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"rowkey\tval_a\t1234"
argument_list|)
decl_stmt|;
name|Pair
argument_list|<
name|Integer
argument_list|,
name|Integer
argument_list|>
name|rowKeyOffsets
init|=
name|parser
operator|.
name|parseRowKey
argument_list|(
name|line
argument_list|,
name|line
operator|.
name|length
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|rowKeyOffsets
operator|.
name|getFirst
argument_list|()
operator|.
name|intValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|6
argument_list|,
name|rowKeyOffsets
operator|.
name|getSecond
argument_list|()
operator|.
name|intValue
argument_list|()
argument_list|)
expr_stmt|;
try|try
block|{
name|line
operator|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"\t\tval_a\t1234"
argument_list|)
expr_stmt|;
name|parser
operator|.
name|parseRowKey
argument_list|(
name|line
argument_list|,
name|line
operator|.
name|length
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"Should get BadTsvLineException on empty rowkey."
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|BadTsvLineException
name|b
parameter_list|)
block|{      }
name|parser
operator|=
operator|new
name|TsvParser
argument_list|(
literal|"col_a,HBASE_ROW_KEY,HBASE_TS_KEY"
argument_list|,
literal|"\t"
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|parser
operator|.
name|getRowKeyColumnIndex
argument_list|()
argument_list|)
expr_stmt|;
name|line
operator|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"val_a\trowkey\t1234"
argument_list|)
expr_stmt|;
name|rowKeyOffsets
operator|=
name|parser
operator|.
name|parseRowKey
argument_list|(
name|line
argument_list|,
name|line
operator|.
name|length
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|6
argument_list|,
name|rowKeyOffsets
operator|.
name|getFirst
argument_list|()
operator|.
name|intValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|6
argument_list|,
name|rowKeyOffsets
operator|.
name|getSecond
argument_list|()
operator|.
name|intValue
argument_list|()
argument_list|)
expr_stmt|;
try|try
block|{
name|line
operator|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"val_a"
argument_list|)
expr_stmt|;
name|rowKeyOffsets
operator|=
name|parser
operator|.
name|parseRowKey
argument_list|(
name|line
argument_list|,
name|line
operator|.
name|length
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"Should get BadTsvLineException when number of columns less than rowkey position."
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|BadTsvLineException
name|b
parameter_list|)
block|{      }
name|parser
operator|=
operator|new
name|TsvParser
argument_list|(
literal|"col_a,HBASE_TS_KEY,HBASE_ROW_KEY"
argument_list|,
literal|"\t"
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|2
argument_list|,
name|parser
operator|.
name|getRowKeyColumnIndex
argument_list|()
argument_list|)
expr_stmt|;
name|line
operator|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"val_a\t1234\trowkey"
argument_list|)
expr_stmt|;
name|rowKeyOffsets
operator|=
name|parser
operator|.
name|parseRowKey
argument_list|(
name|line
argument_list|,
name|line
operator|.
name|length
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|11
argument_list|,
name|rowKeyOffsets
operator|.
name|getFirst
argument_list|()
operator|.
name|intValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|6
argument_list|,
name|rowKeyOffsets
operator|.
name|getSecond
argument_list|()
operator|.
name|intValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testTsvParseAttributesKey
parameter_list|()
throws|throws
name|BadTsvLineException
block|{
name|TsvParser
name|parser
init|=
operator|new
name|TsvParser
argument_list|(
literal|"HBASE_ROW_KEY,col_a,HBASE_TS_KEY,HBASE_ATTRIBUTES_KEY"
argument_list|,
literal|"\t"
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|parser
operator|.
name|getRowKeyColumnIndex
argument_list|()
argument_list|)
expr_stmt|;
name|byte
index|[]
name|line
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"rowkey\tval_a\t1234\tkey=>value"
argument_list|)
decl_stmt|;
name|ParsedLine
name|parse
init|=
name|parser
operator|.
name|parse
argument_list|(
name|line
argument_list|,
name|line
operator|.
name|length
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|18
argument_list|,
name|parse
operator|.
name|getAttributeKeyOffset
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|3
argument_list|,
name|parser
operator|.
name|getAttributesKeyColumnIndex
argument_list|()
argument_list|)
expr_stmt|;
name|String
name|attributes
index|[]
init|=
name|parse
operator|.
name|getIndividualAttributes
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
name|attributes
index|[
literal|0
index|]
argument_list|,
literal|"key=>value"
argument_list|)
expr_stmt|;
try|try
block|{
name|line
operator|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"rowkey\tval_a\t1234"
argument_list|)
expr_stmt|;
name|parser
operator|.
name|parse
argument_list|(
name|line
argument_list|,
name|line
operator|.
name|length
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"Should get BadTsvLineException on empty rowkey."
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|BadTsvLineException
name|b
parameter_list|)
block|{      }
name|parser
operator|=
operator|new
name|TsvParser
argument_list|(
literal|"HBASE_ATTRIBUTES_KEY,col_a,HBASE_ROW_KEY,HBASE_TS_KEY"
argument_list|,
literal|"\t"
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|2
argument_list|,
name|parser
operator|.
name|getRowKeyColumnIndex
argument_list|()
argument_list|)
expr_stmt|;
name|line
operator|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"key=>value\tval_a\trowkey\t1234"
argument_list|)
expr_stmt|;
name|parse
operator|=
name|parser
operator|.
name|parse
argument_list|(
name|line
argument_list|,
name|line
operator|.
name|length
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|parse
operator|.
name|getAttributeKeyOffset
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|parser
operator|.
name|getAttributesKeyColumnIndex
argument_list|()
argument_list|)
expr_stmt|;
name|attributes
operator|=
name|parse
operator|.
name|getIndividualAttributes
argument_list|()
expr_stmt|;
name|assertEquals
argument_list|(
name|attributes
index|[
literal|0
index|]
argument_list|,
literal|"key=>value"
argument_list|)
expr_stmt|;
try|try
block|{
name|line
operator|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"val_a"
argument_list|)
expr_stmt|;
name|ParsedLine
name|parse2
init|=
name|parser
operator|.
name|parse
argument_list|(
name|line
argument_list|,
name|line
operator|.
name|length
argument_list|)
decl_stmt|;
name|fail
argument_list|(
literal|"Should get BadTsvLineException when number of columns less than rowkey position."
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|BadTsvLineException
name|b
parameter_list|)
block|{      }
name|parser
operator|=
operator|new
name|TsvParser
argument_list|(
literal|"col_a,HBASE_ATTRIBUTES_KEY,HBASE_TS_KEY,HBASE_ROW_KEY"
argument_list|,
literal|"\t"
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|3
argument_list|,
name|parser
operator|.
name|getRowKeyColumnIndex
argument_list|()
argument_list|)
expr_stmt|;
name|line
operator|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"val_a\tkey0=>value0,key1=>value1,key2=>value2\t1234\trowkey"
argument_list|)
expr_stmt|;
name|parse
operator|=
name|parser
operator|.
name|parse
argument_list|(
name|line
argument_list|,
name|line
operator|.
name|length
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|parser
operator|.
name|getAttributesKeyColumnIndex
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|6
argument_list|,
name|parse
operator|.
name|getAttributeKeyOffset
argument_list|()
argument_list|)
expr_stmt|;
name|String
index|[]
name|attr
init|=
name|parse
operator|.
name|getIndividualAttributes
argument_list|()
decl_stmt|;
name|int
name|i
init|=
literal|0
decl_stmt|;
for|for
control|(
name|String
name|str
range|:
name|attr
control|)
block|{
name|assertEquals
argument_list|(
operator|(
literal|"key"
operator|+
name|i
operator|+
literal|"=>"
operator|+
literal|"value"
operator|+
name|i
operator|)
argument_list|,
name|str
argument_list|)
expr_stmt|;
name|i
operator|++
expr_stmt|;
block|}
block|}
annotation|@
name|Test
specifier|public
name|void
name|testTsvParserWithCellVisibilityCol
parameter_list|()
throws|throws
name|BadTsvLineException
block|{
name|TsvParser
name|parser
init|=
operator|new
name|TsvParser
argument_list|(
literal|"HBASE_ROW_KEY,col_a,HBASE_TS_KEY,HBASE_ATTRIBUTES_KEY,HBASE_CELL_VISIBILITY"
argument_list|,
literal|"\t"
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|parser
operator|.
name|getRowKeyColumnIndex
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|4
argument_list|,
name|parser
operator|.
name|getCellVisibilityColumnIndex
argument_list|()
argument_list|)
expr_stmt|;
name|byte
index|[]
name|line
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"rowkey\tval_a\t1234\tkey=>value\tPRIVATE&SECRET"
argument_list|)
decl_stmt|;
name|ParsedLine
name|parse
init|=
name|parser
operator|.
name|parse
argument_list|(
name|line
argument_list|,
name|line
operator|.
name|length
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|18
argument_list|,
name|parse
operator|.
name|getAttributeKeyOffset
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|3
argument_list|,
name|parser
operator|.
name|getAttributesKeyColumnIndex
argument_list|()
argument_list|)
expr_stmt|;
name|String
name|attributes
index|[]
init|=
name|parse
operator|.
name|getIndividualAttributes
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
name|attributes
index|[
literal|0
index|]
argument_list|,
literal|"key=>value"
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|29
argument_list|,
name|parse
operator|.
name|getCellVisibilityColumnOffset
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

