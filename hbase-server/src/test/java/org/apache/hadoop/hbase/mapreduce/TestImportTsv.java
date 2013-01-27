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
name|mapreduce
package|;
end_package

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|UnsupportedEncodingException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|List
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
name|commons
operator|.
name|logging
operator|.
name|Log
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|logging
operator|.
name|LogFactory
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
name|*
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
name|mapreduce
operator|.
name|Job
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
name|fs
operator|.
name|FSDataOutputStream
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
name|fs
operator|.
name|Path
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
name|fs
operator|.
name|FileSystem
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
name|conf
operator|.
name|Configuration
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
name|util
operator|.
name|GenericOptionsParser
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
name|client
operator|.
name|HTable
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
name|client
operator|.
name|ResultScanner
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
name|client
operator|.
name|Scan
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
name|client
operator|.
name|HBaseAdmin
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
name|client
operator|.
name|Result
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

begin_class
annotation|@
name|Category
argument_list|(
name|MediumTests
operator|.
name|class
argument_list|)
specifier|public
class|class
name|TestImportTsv
block|{
specifier|private
specifier|static
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|TestImportTsv
operator|.
name|class
argument_list|)
decl_stmt|;
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
argument_list|<
name|String
argument_list|>
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
name|testMROnTable
parameter_list|()
throws|throws
name|Exception
block|{
name|String
name|TABLE_NAME
init|=
literal|"TestTable"
decl_stmt|;
name|String
name|FAMILY
init|=
literal|"FAM"
decl_stmt|;
name|String
name|INPUT_FILE
init|=
literal|"InputFile.esv"
decl_stmt|;
comment|// Prepare the arguments required for the test.
name|String
index|[]
name|args
init|=
operator|new
name|String
index|[]
block|{
literal|"-D"
operator|+
name|ImportTsv
operator|.
name|COLUMNS_CONF_KEY
operator|+
literal|"=HBASE_ROW_KEY,FAM:A,FAM:B"
block|,
literal|"-D"
operator|+
name|ImportTsv
operator|.
name|SEPARATOR_CONF_KEY
operator|+
literal|"=\u001b"
block|,
name|TABLE_NAME
block|,
name|INPUT_FILE
block|}
decl_stmt|;
name|doMROnTableTest
argument_list|(
name|INPUT_FILE
argument_list|,
name|FAMILY
argument_list|,
name|TABLE_NAME
argument_list|,
literal|null
argument_list|,
name|args
argument_list|,
literal|1
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testMROnTableWithTimestamp
parameter_list|()
throws|throws
name|Exception
block|{
name|String
name|TABLE_NAME
init|=
literal|"TestTable"
decl_stmt|;
name|String
name|FAMILY
init|=
literal|"FAM"
decl_stmt|;
name|String
name|INPUT_FILE
init|=
literal|"InputFile1.csv"
decl_stmt|;
comment|// Prepare the arguments required for the test.
name|String
index|[]
name|args
init|=
operator|new
name|String
index|[]
block|{
literal|"-D"
operator|+
name|ImportTsv
operator|.
name|COLUMNS_CONF_KEY
operator|+
literal|"=HBASE_ROW_KEY,HBASE_TS_KEY,FAM:A,FAM:B"
block|,
literal|"-D"
operator|+
name|ImportTsv
operator|.
name|SEPARATOR_CONF_KEY
operator|+
literal|"=,"
block|,
name|TABLE_NAME
block|,
name|INPUT_FILE
block|}
decl_stmt|;
name|String
name|data
init|=
literal|"KEY,1234,VALUE1,VALUE2\n"
decl_stmt|;
name|doMROnTableTest
argument_list|(
name|INPUT_FILE
argument_list|,
name|FAMILY
argument_list|,
name|TABLE_NAME
argument_list|,
name|data
argument_list|,
name|args
argument_list|,
literal|1
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testMROnTableWithCustomMapper
parameter_list|()
throws|throws
name|Exception
block|{
name|String
name|TABLE_NAME
init|=
literal|"TestTable"
decl_stmt|;
name|String
name|FAMILY
init|=
literal|"FAM"
decl_stmt|;
name|String
name|INPUT_FILE
init|=
literal|"InputFile2.esv"
decl_stmt|;
comment|// Prepare the arguments required for the test.
name|String
index|[]
name|args
init|=
operator|new
name|String
index|[]
block|{
literal|"-D"
operator|+
name|ImportTsv
operator|.
name|MAPPER_CONF_KEY
operator|+
literal|"=org.apache.hadoop.hbase.mapreduce.TsvImporterCustomTestMapper"
block|,
name|TABLE_NAME
block|,
name|INPUT_FILE
block|}
decl_stmt|;
name|doMROnTableTest
argument_list|(
name|INPUT_FILE
argument_list|,
name|FAMILY
argument_list|,
name|TABLE_NAME
argument_list|,
literal|null
argument_list|,
name|args
argument_list|,
literal|3
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|doMROnTableTest
parameter_list|(
name|String
name|inputFile
parameter_list|,
name|String
name|family
parameter_list|,
name|String
name|tableName
parameter_list|,
name|String
name|data
parameter_list|,
name|String
index|[]
name|args
parameter_list|,
name|int
name|valueMultiplier
parameter_list|)
throws|throws
name|Exception
block|{
comment|// Cluster
name|HBaseTestingUtility
name|htu1
init|=
operator|new
name|HBaseTestingUtility
argument_list|()
decl_stmt|;
name|htu1
operator|.
name|startMiniCluster
argument_list|()
expr_stmt|;
name|htu1
operator|.
name|startMiniMapReduceCluster
argument_list|()
expr_stmt|;
name|GenericOptionsParser
name|opts
init|=
operator|new
name|GenericOptionsParser
argument_list|(
name|htu1
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|args
argument_list|)
decl_stmt|;
name|Configuration
name|conf
init|=
name|opts
operator|.
name|getConfiguration
argument_list|()
decl_stmt|;
name|args
operator|=
name|opts
operator|.
name|getRemainingArgs
argument_list|()
expr_stmt|;
try|try
block|{
name|FileSystem
name|fs
init|=
name|FileSystem
operator|.
name|get
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|FSDataOutputStream
name|op
init|=
name|fs
operator|.
name|create
argument_list|(
operator|new
name|Path
argument_list|(
name|inputFile
argument_list|)
argument_list|,
literal|true
argument_list|)
decl_stmt|;
if|if
condition|(
name|data
operator|==
literal|null
condition|)
block|{
name|data
operator|=
literal|"KEY\u001bVALUE1\u001bVALUE2\n"
expr_stmt|;
block|}
name|op
operator|.
name|write
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|data
argument_list|)
argument_list|)
expr_stmt|;
name|op
operator|.
name|close
argument_list|()
expr_stmt|;
specifier|final
name|byte
index|[]
name|FAM
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
name|family
argument_list|)
decl_stmt|;
specifier|final
name|byte
index|[]
name|TAB
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
name|tableName
argument_list|)
decl_stmt|;
if|if
condition|(
name|conf
operator|.
name|get
argument_list|(
name|ImportTsv
operator|.
name|BULK_OUTPUT_CONF_KEY
argument_list|)
operator|==
literal|null
condition|)
block|{
name|HTableDescriptor
name|desc
init|=
operator|new
name|HTableDescriptor
argument_list|(
name|TAB
argument_list|)
decl_stmt|;
name|desc
operator|.
name|addFamily
argument_list|(
operator|new
name|HColumnDescriptor
argument_list|(
name|FAM
argument_list|)
argument_list|)
expr_stmt|;
name|HBaseAdmin
name|admin
init|=
operator|new
name|HBaseAdmin
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|admin
operator|.
name|createTable
argument_list|(
name|desc
argument_list|)
expr_stmt|;
name|admin
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
else|else
block|{
comment|// set the hbaseAdmin as we are not going through main()
name|LOG
operator|.
name|info
argument_list|(
literal|"set the hbaseAdmin"
argument_list|)
expr_stmt|;
name|ImportTsv
operator|.
name|createHbaseAdmin
argument_list|(
name|conf
argument_list|)
expr_stmt|;
block|}
name|Job
name|job
init|=
name|ImportTsv
operator|.
name|createSubmittableJob
argument_list|(
name|conf
argument_list|,
name|args
argument_list|)
decl_stmt|;
name|job
operator|.
name|waitForCompletion
argument_list|(
literal|false
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|job
operator|.
name|isSuccessful
argument_list|()
argument_list|)
expr_stmt|;
name|HTable
name|table
init|=
operator|new
name|HTable
argument_list|(
operator|new
name|Configuration
argument_list|(
name|conf
argument_list|)
argument_list|,
name|TAB
argument_list|)
decl_stmt|;
name|boolean
name|verified
init|=
literal|false
decl_stmt|;
name|long
name|pause
init|=
name|conf
operator|.
name|getLong
argument_list|(
literal|"hbase.client.pause"
argument_list|,
literal|5
operator|*
literal|1000
argument_list|)
decl_stmt|;
name|int
name|numRetries
init|=
name|conf
operator|.
name|getInt
argument_list|(
literal|"hbase.client.retries.number"
argument_list|,
literal|5
argument_list|)
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
name|numRetries
condition|;
name|i
operator|++
control|)
block|{
try|try
block|{
name|Scan
name|scan
init|=
operator|new
name|Scan
argument_list|()
decl_stmt|;
comment|// Scan entire family.
name|scan
operator|.
name|addFamily
argument_list|(
name|FAM
argument_list|)
expr_stmt|;
name|ResultScanner
name|resScanner
init|=
name|table
operator|.
name|getScanner
argument_list|(
name|scan
argument_list|)
decl_stmt|;
for|for
control|(
name|Result
name|res
range|:
name|resScanner
control|)
block|{
name|assertTrue
argument_list|(
name|res
operator|.
name|size
argument_list|()
operator|==
literal|2
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|KeyValue
argument_list|>
name|kvs
init|=
name|res
operator|.
name|list
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
name|toU8Str
argument_list|(
name|kvs
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getRow
argument_list|()
argument_list|)
argument_list|,
name|toU8Str
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"KEY"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|toU8Str
argument_list|(
name|kvs
operator|.
name|get
argument_list|(
literal|1
argument_list|)
operator|.
name|getRow
argument_list|()
argument_list|)
argument_list|,
name|toU8Str
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"KEY"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|toU8Str
argument_list|(
name|kvs
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getValue
argument_list|()
argument_list|)
argument_list|,
name|toU8Str
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"VALUE"
operator|+
name|valueMultiplier
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|toU8Str
argument_list|(
name|kvs
operator|.
name|get
argument_list|(
literal|1
argument_list|)
operator|.
name|getValue
argument_list|()
argument_list|)
argument_list|,
name|toU8Str
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"VALUE"
operator|+
literal|2
operator|*
name|valueMultiplier
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
comment|// Only one result set is expected, so let it loop.
block|}
name|verified
operator|=
literal|true
expr_stmt|;
break|break;
block|}
catch|catch
parameter_list|(
name|NullPointerException
name|e
parameter_list|)
block|{
comment|// If here, a cell was empty.  Presume its because updates came in
comment|// after the scanner had been opened.  Wait a while and retry.
block|}
try|try
block|{
name|Thread
operator|.
name|sleep
argument_list|(
name|pause
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|e
parameter_list|)
block|{
comment|// continue
block|}
block|}
name|table
operator|.
name|close
argument_list|()
expr_stmt|;
name|assertTrue
argument_list|(
name|verified
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|htu1
operator|.
name|shutdownMiniMapReduceCluster
argument_list|()
expr_stmt|;
name|htu1
operator|.
name|shutdownMiniCluster
argument_list|()
expr_stmt|;
block|}
block|}
annotation|@
name|Test
specifier|public
name|void
name|testBulkOutputWithoutAnExistingTable
parameter_list|()
throws|throws
name|Exception
block|{
name|String
name|TABLE_NAME
init|=
literal|"TestTable"
decl_stmt|;
name|String
name|FAMILY
init|=
literal|"FAM"
decl_stmt|;
name|String
name|INPUT_FILE
init|=
literal|"InputFile2.esv"
decl_stmt|;
comment|// Prepare the arguments required for the test.
name|String
index|[]
name|args
init|=
operator|new
name|String
index|[]
block|{
literal|"-D"
operator|+
name|ImportTsv
operator|.
name|COLUMNS_CONF_KEY
operator|+
literal|"=HBASE_ROW_KEY,FAM:A,FAM:B"
block|,
literal|"-D"
operator|+
name|ImportTsv
operator|.
name|SEPARATOR_CONF_KEY
operator|+
literal|"=\u001b"
block|,
literal|"-D"
operator|+
name|ImportTsv
operator|.
name|BULK_OUTPUT_CONF_KEY
operator|+
literal|"=output"
block|,
name|TABLE_NAME
block|,
name|INPUT_FILE
block|}
decl_stmt|;
name|doMROnTableTest
argument_list|(
name|INPUT_FILE
argument_list|,
name|FAMILY
argument_list|,
name|TABLE_NAME
argument_list|,
literal|null
argument_list|,
name|args
argument_list|,
literal|3
argument_list|)
expr_stmt|;
block|}
specifier|public
specifier|static
name|String
name|toU8Str
parameter_list|(
name|byte
index|[]
name|bytes
parameter_list|)
throws|throws
name|UnsupportedEncodingException
block|{
return|return
operator|new
name|String
argument_list|(
name|bytes
argument_list|)
return|;
block|}
block|}
end_class

end_unit

