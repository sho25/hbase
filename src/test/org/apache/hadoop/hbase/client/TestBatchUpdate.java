begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Copyright 2007 The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|client
package|;
end_package

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|IOException
import|;
end_import

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
name|ArrayList
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Arrays
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Map
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
name|HBaseClusterTestCase
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
name|HColumnDescriptor
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
name|HTableDescriptor
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
name|io
operator|.
name|BatchUpdate
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
name|io
operator|.
name|Cell
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
name|io
operator|.
name|RowResult
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

begin_comment
comment|/**  * Test batch updates  */
end_comment

begin_class
specifier|public
class|class
name|TestBatchUpdate
extends|extends
name|HBaseClusterTestCase
block|{
specifier|private
specifier|static
specifier|final
name|String
name|CONTENTS_STR
init|=
literal|"contents:"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|CONTENTS
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
name|CONTENTS_STR
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|SMALLFAM_STR
init|=
literal|"smallfam:"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|SMALLFAM
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
name|SMALLFAM_STR
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|SMALL_LENGTH
init|=
literal|1
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|NB_BATCH_ROWS
init|=
literal|10
decl_stmt|;
specifier|private
name|byte
index|[]
name|value
decl_stmt|;
specifier|private
name|byte
index|[]
name|smallValue
decl_stmt|;
specifier|private
name|HTableDescriptor
name|desc
init|=
literal|null
decl_stmt|;
specifier|private
name|HTable
name|table
init|=
literal|null
decl_stmt|;
comment|/**    * @throws UnsupportedEncodingException    */
specifier|public
name|TestBatchUpdate
parameter_list|()
throws|throws
name|UnsupportedEncodingException
block|{
name|super
argument_list|()
expr_stmt|;
name|value
operator|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"abcd"
argument_list|)
expr_stmt|;
name|smallValue
operator|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"a"
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|setUp
parameter_list|()
throws|throws
name|Exception
block|{
name|super
operator|.
name|setUp
argument_list|()
expr_stmt|;
name|this
operator|.
name|desc
operator|=
operator|new
name|HTableDescriptor
argument_list|(
literal|"test"
argument_list|)
expr_stmt|;
name|desc
operator|.
name|addFamily
argument_list|(
operator|new
name|HColumnDescriptor
argument_list|(
name|CONTENTS_STR
argument_list|)
argument_list|)
expr_stmt|;
name|desc
operator|.
name|addFamily
argument_list|(
operator|new
name|HColumnDescriptor
argument_list|(
name|SMALLFAM
argument_list|,
name|HColumnDescriptor
operator|.
name|DEFAULT_VERSIONS
argument_list|,
name|HColumnDescriptor
operator|.
name|DEFAULT_COMPRESSION
argument_list|,
name|HColumnDescriptor
operator|.
name|DEFAULT_IN_MEMORY
argument_list|,
name|HColumnDescriptor
operator|.
name|DEFAULT_BLOCKCACHE
argument_list|,
name|SMALL_LENGTH
argument_list|,
name|HColumnDescriptor
operator|.
name|DEFAULT_TTL
argument_list|,
name|HColumnDescriptor
operator|.
name|DEFAULT_BLOOMFILTER
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
name|table
operator|=
operator|new
name|HTable
argument_list|(
name|conf
argument_list|,
name|desc
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|testRowsBatchUpdateBufferedOneFlush
parameter_list|()
block|{
name|table
operator|.
name|setAutoFlush
argument_list|(
literal|false
argument_list|)
expr_stmt|;
name|ArrayList
argument_list|<
name|BatchUpdate
argument_list|>
name|rowsUpdate
init|=
operator|new
name|ArrayList
argument_list|<
name|BatchUpdate
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
name|NB_BATCH_ROWS
operator|*
literal|10
condition|;
name|i
operator|++
control|)
block|{
name|BatchUpdate
name|batchUpdate
init|=
operator|new
name|BatchUpdate
argument_list|(
literal|"row"
operator|+
name|i
argument_list|)
decl_stmt|;
name|batchUpdate
operator|.
name|put
argument_list|(
name|CONTENTS
argument_list|,
name|value
argument_list|)
expr_stmt|;
name|rowsUpdate
operator|.
name|add
argument_list|(
name|batchUpdate
argument_list|)
expr_stmt|;
block|}
try|try
block|{
name|table
operator|.
name|commit
argument_list|(
name|rowsUpdate
argument_list|)
expr_stmt|;
name|byte
index|[]
index|[]
name|columns
init|=
block|{
name|CONTENTS
block|}
decl_stmt|;
name|Scanner
name|scanner
init|=
name|table
operator|.
name|getScanner
argument_list|(
name|columns
argument_list|,
name|HConstants
operator|.
name|EMPTY_START_ROW
argument_list|)
decl_stmt|;
name|int
name|nbRows
init|=
literal|0
decl_stmt|;
for|for
control|(
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unused"
argument_list|)
name|RowResult
name|row
range|:
name|scanner
control|)
name|nbRows
operator|++
expr_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|nbRows
argument_list|)
expr_stmt|;
name|scanner
operator|.
name|close
argument_list|()
expr_stmt|;
name|table
operator|.
name|flushCommits
argument_list|()
expr_stmt|;
name|scanner
operator|=
name|table
operator|.
name|getScanner
argument_list|(
name|columns
argument_list|,
name|HConstants
operator|.
name|EMPTY_START_ROW
argument_list|)
expr_stmt|;
name|nbRows
operator|=
literal|0
expr_stmt|;
for|for
control|(
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unused"
argument_list|)
name|RowResult
name|row
range|:
name|scanner
control|)
name|nbRows
operator|++
expr_stmt|;
name|assertEquals
argument_list|(
name|NB_BATCH_ROWS
operator|*
literal|10
argument_list|,
name|nbRows
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|fail
argument_list|(
literal|"This is unexpected : "
operator|+
name|e
argument_list|)
expr_stmt|;
block|}
block|}
specifier|public
name|void
name|testRowsBatchUpdateBufferedManyManyFlushes
parameter_list|()
block|{
name|table
operator|.
name|setAutoFlush
argument_list|(
literal|false
argument_list|)
expr_stmt|;
name|table
operator|.
name|setWriteBufferSize
argument_list|(
literal|10
argument_list|)
expr_stmt|;
name|ArrayList
argument_list|<
name|BatchUpdate
argument_list|>
name|rowsUpdate
init|=
operator|new
name|ArrayList
argument_list|<
name|BatchUpdate
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
name|NB_BATCH_ROWS
operator|*
literal|10
condition|;
name|i
operator|++
control|)
block|{
name|BatchUpdate
name|batchUpdate
init|=
operator|new
name|BatchUpdate
argument_list|(
literal|"row"
operator|+
name|i
argument_list|)
decl_stmt|;
name|batchUpdate
operator|.
name|put
argument_list|(
name|CONTENTS
argument_list|,
name|value
argument_list|)
expr_stmt|;
name|rowsUpdate
operator|.
name|add
argument_list|(
name|batchUpdate
argument_list|)
expr_stmt|;
block|}
try|try
block|{
name|table
operator|.
name|commit
argument_list|(
name|rowsUpdate
argument_list|)
expr_stmt|;
name|table
operator|.
name|flushCommits
argument_list|()
expr_stmt|;
name|byte
index|[]
index|[]
name|columns
init|=
block|{
name|CONTENTS
block|}
decl_stmt|;
name|Scanner
name|scanner
init|=
name|table
operator|.
name|getScanner
argument_list|(
name|columns
argument_list|,
name|HConstants
operator|.
name|EMPTY_START_ROW
argument_list|)
decl_stmt|;
name|int
name|nbRows
init|=
literal|0
decl_stmt|;
for|for
control|(
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unused"
argument_list|)
name|RowResult
name|row
range|:
name|scanner
control|)
name|nbRows
operator|++
expr_stmt|;
name|assertEquals
argument_list|(
name|NB_BATCH_ROWS
operator|*
literal|10
argument_list|,
name|nbRows
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|fail
argument_list|(
literal|"This is unexpected : "
operator|+
name|e
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * @throws IOException    */
specifier|public
name|void
name|testBatchUpdate
parameter_list|()
throws|throws
name|IOException
block|{
name|BatchUpdate
name|bu
init|=
operator|new
name|BatchUpdate
argument_list|(
literal|"row1"
argument_list|)
decl_stmt|;
name|bu
operator|.
name|put
argument_list|(
name|CONTENTS
argument_list|,
name|value
argument_list|)
expr_stmt|;
comment|// Can't do this in 0.20.0 mix and match put and delete -- bu.delete(CONTENTS);
name|table
operator|.
name|commit
argument_list|(
name|bu
argument_list|)
expr_stmt|;
name|bu
operator|=
operator|new
name|BatchUpdate
argument_list|(
literal|"row2"
argument_list|)
expr_stmt|;
name|bu
operator|.
name|put
argument_list|(
name|CONTENTS
argument_list|,
name|value
argument_list|)
expr_stmt|;
name|byte
index|[]
index|[]
name|getColumns
init|=
name|bu
operator|.
name|getColumns
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
name|getColumns
operator|.
name|length
argument_list|,
literal|1
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|Arrays
operator|.
name|equals
argument_list|(
name|getColumns
index|[
literal|0
index|]
argument_list|,
name|CONTENTS
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|bu
operator|.
name|hasColumn
argument_list|(
name|CONTENTS
argument_list|)
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|bu
operator|.
name|hasColumn
argument_list|(
operator|new
name|byte
index|[]
block|{}
argument_list|)
argument_list|)
expr_stmt|;
name|byte
index|[]
name|getValue
init|=
name|bu
operator|.
name|get
argument_list|(
name|getColumns
index|[
literal|0
index|]
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|Arrays
operator|.
name|equals
argument_list|(
name|getValue
argument_list|,
name|value
argument_list|)
argument_list|)
expr_stmt|;
name|table
operator|.
name|commit
argument_list|(
name|bu
argument_list|)
expr_stmt|;
name|byte
index|[]
index|[]
name|columns
init|=
block|{
name|CONTENTS
block|}
decl_stmt|;
name|Scanner
name|scanner
init|=
name|table
operator|.
name|getScanner
argument_list|(
name|columns
argument_list|,
name|HConstants
operator|.
name|EMPTY_START_ROW
argument_list|)
decl_stmt|;
for|for
control|(
name|RowResult
name|r
range|:
name|scanner
control|)
block|{
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|byte
index|[]
argument_list|,
name|Cell
argument_list|>
name|e
range|:
name|r
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
name|Bytes
operator|.
name|toString
argument_list|(
name|r
operator|.
name|getRow
argument_list|()
argument_list|)
operator|+
literal|": row: "
operator|+
name|e
operator|.
name|getKey
argument_list|()
operator|+
literal|" value: "
operator|+
operator|new
name|String
argument_list|(
name|e
operator|.
name|getValue
argument_list|()
operator|.
name|getValue
argument_list|()
argument_list|,
name|HConstants
operator|.
name|UTF8_ENCODING
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
specifier|public
name|void
name|testRowsBatchUpdate
parameter_list|()
block|{
name|ArrayList
argument_list|<
name|BatchUpdate
argument_list|>
name|rowsUpdate
init|=
operator|new
name|ArrayList
argument_list|<
name|BatchUpdate
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
name|NB_BATCH_ROWS
condition|;
name|i
operator|++
control|)
block|{
name|BatchUpdate
name|batchUpdate
init|=
operator|new
name|BatchUpdate
argument_list|(
literal|"row"
operator|+
name|i
argument_list|)
decl_stmt|;
name|batchUpdate
operator|.
name|put
argument_list|(
name|CONTENTS
argument_list|,
name|value
argument_list|)
expr_stmt|;
name|rowsUpdate
operator|.
name|add
argument_list|(
name|batchUpdate
argument_list|)
expr_stmt|;
block|}
try|try
block|{
name|table
operator|.
name|commit
argument_list|(
name|rowsUpdate
argument_list|)
expr_stmt|;
name|byte
index|[]
index|[]
name|columns
init|=
block|{
name|CONTENTS
block|}
decl_stmt|;
name|Scanner
name|scanner
init|=
name|table
operator|.
name|getScanner
argument_list|(
name|columns
argument_list|,
name|HConstants
operator|.
name|EMPTY_START_ROW
argument_list|)
decl_stmt|;
name|int
name|nbRows
init|=
literal|0
decl_stmt|;
for|for
control|(
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unused"
argument_list|)
name|RowResult
name|row
range|:
name|scanner
control|)
name|nbRows
operator|++
expr_stmt|;
name|assertEquals
argument_list|(
name|NB_BATCH_ROWS
argument_list|,
name|nbRows
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|fail
argument_list|(
literal|"This is unexpected : "
operator|+
name|e
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

