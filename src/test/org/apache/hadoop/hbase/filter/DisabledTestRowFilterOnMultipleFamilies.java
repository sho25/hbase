begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Copyright 2008 The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|io
operator|.
name|IOException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|HashMap
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
name|junit
operator|.
name|framework
operator|.
name|Assert
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
name|KeyValue
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
name|Put
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
comment|/**  * Test for regexp filters (HBASE-527)  */
end_comment

begin_class
specifier|public
class|class
name|DisabledTestRowFilterOnMultipleFamilies
extends|extends
name|HBaseClusterTestCase
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
name|DisabledTestRowFilterOnMultipleFamilies
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
specifier|static
specifier|final
name|String
name|TABLE_NAME
init|=
literal|"TestTable"
decl_stmt|;
specifier|static
specifier|final
name|String
name|COLUMN1
init|=
literal|"A:col1"
decl_stmt|;
specifier|static
specifier|final
name|byte
index|[]
name|TEXT_COLUMN1
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
name|COLUMN1
argument_list|)
decl_stmt|;
specifier|static
specifier|final
name|String
name|COLUMN2
init|=
literal|"B:col2"
decl_stmt|;
specifier|static
specifier|final
name|byte
index|[]
name|TEXT_COLUMN2
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
name|COLUMN2
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
index|[]
name|columns
init|=
block|{
name|TEXT_COLUMN1
block|,
name|TEXT_COLUMN2
block|}
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|NUM_ROWS
init|=
literal|10
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|VALUE
init|=
literal|"HELLO"
operator|.
name|getBytes
argument_list|()
decl_stmt|;
comment|/** @throws IOException */
specifier|public
name|void
name|testMultipleFamilies
parameter_list|()
throws|throws
name|IOException
block|{
name|HTableDescriptor
name|desc
init|=
operator|new
name|HTableDescriptor
argument_list|(
name|TABLE_NAME
argument_list|)
decl_stmt|;
name|desc
operator|.
name|addFamily
argument_list|(
operator|new
name|HColumnDescriptor
argument_list|(
literal|"A:"
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
literal|"B:"
argument_list|)
argument_list|)
expr_stmt|;
comment|// Create a table.
name|HBaseAdmin
name|admin
init|=
operator|new
name|HBaseAdmin
argument_list|(
name|this
operator|.
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
comment|// insert some data into the test table
name|HTable
name|table
init|=
operator|new
name|HTable
argument_list|(
name|conf
argument_list|,
name|TABLE_NAME
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
name|NUM_ROWS
condition|;
name|i
operator|++
control|)
block|{
name|Put
name|put
init|=
operator|new
name|Put
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"row_"
operator|+
name|String
operator|.
name|format
argument_list|(
literal|"%1$05d"
argument_list|,
name|i
argument_list|)
argument_list|)
argument_list|)
decl_stmt|;
name|byte
index|[]
index|[]
name|famAndQf
init|=
name|KeyValue
operator|.
name|parseColumn
argument_list|(
name|TEXT_COLUMN1
argument_list|)
decl_stmt|;
name|put
operator|.
name|add
argument_list|(
name|famAndQf
index|[
literal|0
index|]
argument_list|,
name|famAndQf
index|[
literal|1
index|]
argument_list|,
name|VALUE
argument_list|)
expr_stmt|;
name|famAndQf
operator|=
name|KeyValue
operator|.
name|parseColumn
argument_list|(
name|TEXT_COLUMN2
argument_list|)
expr_stmt|;
name|put
operator|.
name|add
argument_list|(
name|famAndQf
index|[
literal|0
index|]
argument_list|,
name|famAndQf
index|[
literal|1
index|]
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
name|String
operator|.
name|format
argument_list|(
literal|"%1$05d"
argument_list|,
name|i
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|table
operator|.
name|put
argument_list|(
name|put
argument_list|)
expr_stmt|;
block|}
name|LOG
operator|.
name|info
argument_list|(
literal|"Print table contents using scanner before map/reduce for "
operator|+
name|TABLE_NAME
argument_list|)
expr_stmt|;
name|scanTable
argument_list|(
name|TABLE_NAME
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Print table contents using scanner+filter before map/reduce for "
operator|+
name|TABLE_NAME
argument_list|)
expr_stmt|;
name|scanTableWithRowFilter
argument_list|(
name|TABLE_NAME
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|scanTable
parameter_list|(
specifier|final
name|String
name|tableName
parameter_list|,
specifier|final
name|boolean
name|printValues
parameter_list|)
throws|throws
name|IOException
block|{
name|HTable
name|table
init|=
operator|new
name|HTable
argument_list|(
name|conf
argument_list|,
name|tableName
argument_list|)
decl_stmt|;
name|Scan
name|scan
init|=
operator|new
name|Scan
argument_list|()
decl_stmt|;
name|scan
operator|.
name|addColumns
argument_list|(
name|columns
argument_list|)
expr_stmt|;
name|ResultScanner
name|scanner
init|=
name|table
operator|.
name|getScanner
argument_list|(
name|scan
argument_list|)
decl_stmt|;
name|int
name|numFound
init|=
name|doScan
argument_list|(
name|scanner
argument_list|,
name|printValues
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|NUM_ROWS
argument_list|,
name|numFound
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|scanTableWithRowFilter
parameter_list|(
specifier|final
name|String
name|tableName
parameter_list|,
specifier|final
name|boolean
name|printValues
parameter_list|)
throws|throws
name|IOException
block|{
name|HTable
name|table
init|=
operator|new
name|HTable
argument_list|(
name|conf
argument_list|,
name|tableName
argument_list|)
decl_stmt|;
name|Map
argument_list|<
name|byte
index|[]
argument_list|,
name|Cell
argument_list|>
name|columnMap
init|=
operator|new
name|HashMap
argument_list|<
name|byte
index|[]
argument_list|,
name|Cell
argument_list|>
argument_list|()
decl_stmt|;
name|columnMap
operator|.
name|put
argument_list|(
name|TEXT_COLUMN1
argument_list|,
operator|new
name|Cell
argument_list|(
name|VALUE
argument_list|,
name|HConstants
operator|.
name|LATEST_TIMESTAMP
argument_list|)
argument_list|)
expr_stmt|;
name|RegExpRowFilter
name|filter
init|=
operator|new
name|RegExpRowFilter
argument_list|(
literal|null
argument_list|,
name|columnMap
argument_list|)
decl_stmt|;
name|Scan
name|scan
init|=
operator|new
name|Scan
argument_list|()
decl_stmt|;
name|scan
operator|.
name|addColumns
argument_list|(
name|columns
argument_list|)
expr_stmt|;
comment|//    scan.setFilter(filter);
name|ResultScanner
name|scanner
init|=
name|table
operator|.
name|getScanner
argument_list|(
name|scan
argument_list|)
decl_stmt|;
name|int
name|numFound
init|=
name|doScan
argument_list|(
name|scanner
argument_list|,
name|printValues
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|NUM_ROWS
argument_list|,
name|numFound
argument_list|)
expr_stmt|;
block|}
specifier|private
name|int
name|doScan
parameter_list|(
specifier|final
name|ResultScanner
name|scanner
parameter_list|,
specifier|final
name|boolean
name|printValues
parameter_list|)
throws|throws
name|IOException
block|{
block|{
name|int
name|count
init|=
literal|0
decl_stmt|;
try|try
block|{
for|for
control|(
name|Result
name|result
range|:
name|scanner
control|)
block|{
if|if
condition|(
name|printValues
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"row: "
operator|+
name|Bytes
operator|.
name|toString
argument_list|(
name|result
operator|.
name|getRow
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
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
name|result
operator|.
name|getRowResult
argument_list|()
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|" column: "
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
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|2
argument_list|,
name|result
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|count
operator|++
expr_stmt|;
block|}
block|}
finally|finally
block|{
name|scanner
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
return|return
name|count
return|;
block|}
block|}
block|}
end_class

end_unit

