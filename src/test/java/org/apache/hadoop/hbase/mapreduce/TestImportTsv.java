begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Copyright 2010 The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
specifier|public
class|class
name|TestImportTsv
block|{
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
block|}
end_class

end_unit

