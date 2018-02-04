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
name|security
operator|.
name|visibility
package|;
end_package

begin_import
import|import static
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|security
operator|.
name|visibility
operator|.
name|VisibilityConstants
operator|.
name|LABELS_TABLE_NAME
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
name|security
operator|.
name|PrivilegedExceptionAction
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
name|List
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
name|hbase
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
name|CellScanner
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
name|HBaseTestingUtility
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
name|TableName
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
name|Connection
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
name|ConnectionFactory
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
name|Delete
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
name|Table
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
name|protobuf
operator|.
name|generated
operator|.
name|VisibilityLabelsProtos
operator|.
name|VisibilityLabelsResponse
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
name|security
operator|.
name|User
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
name|AfterClass
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|BeforeClass
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Rule
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
name|rules
operator|.
name|TestName
import|;
end_import

begin_comment
comment|/**  * Tests visibility labels with deletes  */
end_comment

begin_class
specifier|public
specifier|abstract
class|class
name|VisibilityLabelsWithDeletesTestBase
block|{
specifier|protected
specifier|static
specifier|final
name|String
name|TOPSECRET
init|=
literal|"TOPSECRET"
decl_stmt|;
specifier|protected
specifier|static
specifier|final
name|String
name|PUBLIC
init|=
literal|"PUBLIC"
decl_stmt|;
specifier|protected
specifier|static
specifier|final
name|String
name|PRIVATE
init|=
literal|"PRIVATE"
decl_stmt|;
specifier|protected
specifier|static
specifier|final
name|String
name|CONFIDENTIAL
init|=
literal|"CONFIDENTIAL"
decl_stmt|;
specifier|protected
specifier|static
specifier|final
name|String
name|SECRET
init|=
literal|"SECRET"
decl_stmt|;
specifier|protected
specifier|static
specifier|final
name|HBaseTestingUtility
name|TEST_UTIL
init|=
operator|new
name|HBaseTestingUtility
argument_list|()
decl_stmt|;
specifier|protected
specifier|static
specifier|final
name|byte
index|[]
name|row1
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"row1"
argument_list|)
decl_stmt|;
specifier|protected
specifier|static
specifier|final
name|byte
index|[]
name|row2
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"row2"
argument_list|)
decl_stmt|;
specifier|protected
specifier|final
specifier|static
name|byte
index|[]
name|fam
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"info"
argument_list|)
decl_stmt|;
specifier|protected
specifier|final
specifier|static
name|byte
index|[]
name|qual
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"qual"
argument_list|)
decl_stmt|;
specifier|protected
specifier|final
specifier|static
name|byte
index|[]
name|qual1
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"qual1"
argument_list|)
decl_stmt|;
specifier|protected
specifier|final
specifier|static
name|byte
index|[]
name|qual2
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"qual2"
argument_list|)
decl_stmt|;
specifier|protected
specifier|final
specifier|static
name|byte
index|[]
name|value
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"value"
argument_list|)
decl_stmt|;
specifier|protected
specifier|final
specifier|static
name|byte
index|[]
name|value1
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"value1"
argument_list|)
decl_stmt|;
specifier|protected
specifier|static
name|Configuration
name|conf
decl_stmt|;
annotation|@
name|Rule
specifier|public
specifier|final
name|TestName
name|testName
init|=
operator|new
name|TestName
argument_list|()
decl_stmt|;
specifier|protected
specifier|static
name|User
name|SUPERUSER
decl_stmt|;
annotation|@
name|BeforeClass
specifier|public
specifier|static
name|void
name|setupBeforeClass
parameter_list|()
throws|throws
name|Exception
block|{
comment|// setup configuration
name|conf
operator|=
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
expr_stmt|;
name|VisibilityTestUtil
operator|.
name|enableVisiblityLabels
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setClass
argument_list|(
name|VisibilityUtils
operator|.
name|VISIBILITY_LABEL_GENERATOR_CLASS
argument_list|,
name|SimpleScanLabelGenerator
operator|.
name|class
argument_list|,
name|ScanLabelGenerator
operator|.
name|class
argument_list|)
expr_stmt|;
name|conf
operator|.
name|set
argument_list|(
literal|"hbase.superuser"
argument_list|,
literal|"admin"
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|startMiniCluster
argument_list|(
literal|2
argument_list|)
expr_stmt|;
name|SUPERUSER
operator|=
name|User
operator|.
name|createUserForTesting
argument_list|(
name|conf
argument_list|,
literal|"admin"
argument_list|,
operator|new
name|String
index|[]
block|{
literal|"supergroup"
block|}
argument_list|)
expr_stmt|;
comment|// Wait for the labels table to become available
name|TEST_UTIL
operator|.
name|waitTableEnabled
argument_list|(
name|LABELS_TABLE_NAME
operator|.
name|getName
argument_list|()
argument_list|,
literal|50000
argument_list|)
expr_stmt|;
name|addLabels
argument_list|()
expr_stmt|;
block|}
annotation|@
name|AfterClass
specifier|public
specifier|static
name|void
name|tearDownAfterClass
parameter_list|()
throws|throws
name|Exception
block|{
name|TEST_UTIL
operator|.
name|shutdownMiniCluster
argument_list|()
expr_stmt|;
block|}
specifier|public
specifier|static
name|void
name|addLabels
parameter_list|()
throws|throws
name|Exception
block|{
name|PrivilegedExceptionAction
argument_list|<
name|VisibilityLabelsResponse
argument_list|>
name|action
init|=
operator|new
name|PrivilegedExceptionAction
argument_list|<
name|VisibilityLabelsResponse
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|VisibilityLabelsResponse
name|run
parameter_list|()
throws|throws
name|Exception
block|{
name|String
index|[]
name|labels
init|=
block|{
name|SECRET
block|,
name|TOPSECRET
block|,
name|CONFIDENTIAL
block|,
name|PUBLIC
block|,
name|PRIVATE
block|}
decl_stmt|;
try|try
init|(
name|Connection
name|conn
init|=
name|ConnectionFactory
operator|.
name|createConnection
argument_list|(
name|conf
argument_list|)
init|)
block|{
name|VisibilityClient
operator|.
name|addLabels
argument_list|(
name|conn
argument_list|,
name|labels
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Throwable
name|t
parameter_list|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
name|t
argument_list|)
throw|;
block|}
return|return
literal|null
return|;
block|}
block|}
decl_stmt|;
name|SUPERUSER
operator|.
name|runAs
argument_list|(
name|action
argument_list|)
expr_stmt|;
block|}
specifier|protected
specifier|abstract
name|Table
name|createTable
parameter_list|(
name|byte
index|[]
name|fam
parameter_list|)
throws|throws
name|IOException
function_decl|;
specifier|protected
specifier|final
name|void
name|setAuths
parameter_list|()
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{
name|PrivilegedExceptionAction
argument_list|<
name|VisibilityLabelsResponse
argument_list|>
name|action
init|=
operator|new
name|PrivilegedExceptionAction
argument_list|<
name|VisibilityLabelsResponse
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|VisibilityLabelsResponse
name|run
parameter_list|()
throws|throws
name|Exception
block|{
try|try
init|(
name|Connection
name|conn
init|=
name|ConnectionFactory
operator|.
name|createConnection
argument_list|(
name|conf
argument_list|)
init|)
block|{
return|return
name|VisibilityClient
operator|.
name|setAuths
argument_list|(
name|conn
argument_list|,
operator|new
name|String
index|[]
block|{
name|CONFIDENTIAL
block|,
name|PRIVATE
block|,
name|SECRET
block|,
name|TOPSECRET
block|}
argument_list|,
name|SUPERUSER
operator|.
name|getShortName
argument_list|()
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|Throwable
name|e
parameter_list|)
block|{           }
return|return
literal|null
return|;
block|}
block|}
decl_stmt|;
name|SUPERUSER
operator|.
name|runAs
argument_list|(
name|action
argument_list|)
expr_stmt|;
block|}
specifier|private
name|Table
name|createTableAndWriteDataWithLabels
parameter_list|(
name|String
modifier|...
name|labelExps
parameter_list|)
throws|throws
name|Exception
block|{
name|Table
name|table
init|=
name|createTable
argument_list|(
name|fam
argument_list|)
decl_stmt|;
name|int
name|i
init|=
literal|1
decl_stmt|;
name|List
argument_list|<
name|Put
argument_list|>
name|puts
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|(
name|labelExps
operator|.
name|length
argument_list|)
decl_stmt|;
for|for
control|(
name|String
name|labelExp
range|:
name|labelExps
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
literal|"row"
operator|+
name|i
argument_list|)
argument_list|)
decl_stmt|;
name|put
operator|.
name|addColumn
argument_list|(
name|fam
argument_list|,
name|qual
argument_list|,
name|HConstants
operator|.
name|LATEST_TIMESTAMP
argument_list|,
name|value
argument_list|)
expr_stmt|;
name|put
operator|.
name|setCellVisibility
argument_list|(
operator|new
name|CellVisibility
argument_list|(
name|labelExp
argument_list|)
argument_list|)
expr_stmt|;
name|puts
operator|.
name|add
argument_list|(
name|put
argument_list|)
expr_stmt|;
name|table
operator|.
name|put
argument_list|(
name|put
argument_list|)
expr_stmt|;
name|i
operator|++
expr_stmt|;
block|}
comment|// table.put(puts);
return|return
name|table
return|;
block|}
specifier|private
name|Table
name|createTableAndWriteDataWithLabels
parameter_list|(
name|long
index|[]
name|timestamp
parameter_list|,
name|String
modifier|...
name|labelExps
parameter_list|)
throws|throws
name|Exception
block|{
name|Table
name|table
init|=
name|createTable
argument_list|(
name|fam
argument_list|)
decl_stmt|;
name|int
name|i
init|=
literal|1
decl_stmt|;
name|List
argument_list|<
name|Put
argument_list|>
name|puts
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|(
name|labelExps
operator|.
name|length
argument_list|)
decl_stmt|;
for|for
control|(
name|String
name|labelExp
range|:
name|labelExps
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
literal|"row"
operator|+
name|i
argument_list|)
argument_list|)
decl_stmt|;
name|put
operator|.
name|addColumn
argument_list|(
name|fam
argument_list|,
name|qual
argument_list|,
name|timestamp
index|[
name|i
operator|-
literal|1
index|]
argument_list|,
name|value
argument_list|)
expr_stmt|;
name|put
operator|.
name|setCellVisibility
argument_list|(
operator|new
name|CellVisibility
argument_list|(
name|labelExp
argument_list|)
argument_list|)
expr_stmt|;
name|puts
operator|.
name|add
argument_list|(
name|put
argument_list|)
expr_stmt|;
name|table
operator|.
name|put
argument_list|(
name|put
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|getAdmin
argument_list|()
operator|.
name|flush
argument_list|(
name|table
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|i
operator|++
expr_stmt|;
block|}
return|return
name|table
return|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testVisibilityLabelsWithDeleteColumns
parameter_list|()
throws|throws
name|Throwable
block|{
name|setAuths
argument_list|()
expr_stmt|;
specifier|final
name|TableName
name|tableName
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
name|testName
operator|.
name|getMethodName
argument_list|()
argument_list|)
decl_stmt|;
try|try
init|(
name|Table
name|table
init|=
name|createTableAndWriteDataWithLabels
argument_list|(
name|SECRET
operator|+
literal|"&"
operator|+
name|TOPSECRET
argument_list|,
name|SECRET
argument_list|)
init|)
block|{
name|PrivilegedExceptionAction
argument_list|<
name|Void
argument_list|>
name|actiona
init|=
operator|new
name|PrivilegedExceptionAction
argument_list|<
name|Void
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|Void
name|run
parameter_list|()
throws|throws
name|Exception
block|{
try|try
init|(
name|Connection
name|connection
init|=
name|ConnectionFactory
operator|.
name|createConnection
argument_list|(
name|conf
argument_list|)
init|;
name|Table
name|table
operator|=
name|connection
operator|.
name|getTable
argument_list|(
name|tableName
argument_list|)
init|)
block|{
name|Delete
name|d
init|=
operator|new
name|Delete
argument_list|(
name|row1
argument_list|)
decl_stmt|;
name|d
operator|.
name|setCellVisibility
argument_list|(
operator|new
name|CellVisibility
argument_list|(
name|TOPSECRET
operator|+
literal|"&"
operator|+
name|SECRET
argument_list|)
argument_list|)
expr_stmt|;
name|d
operator|.
name|addColumns
argument_list|(
name|fam
argument_list|,
name|qual
argument_list|)
expr_stmt|;
name|table
operator|.
name|delete
argument_list|(
name|d
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Throwable
name|t
parameter_list|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
name|t
argument_list|)
throw|;
block|}
return|return
literal|null
return|;
block|}
block|}
decl_stmt|;
name|SUPERUSER
operator|.
name|runAs
argument_list|(
name|actiona
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|getAdmin
argument_list|()
operator|.
name|flush
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
name|Scan
name|s
init|=
operator|new
name|Scan
argument_list|()
decl_stmt|;
name|s
operator|.
name|setAuthorizations
argument_list|(
operator|new
name|Authorizations
argument_list|(
name|SECRET
argument_list|,
name|PRIVATE
argument_list|,
name|CONFIDENTIAL
argument_list|)
argument_list|)
expr_stmt|;
name|ResultScanner
name|scanner
init|=
name|table
operator|.
name|getScanner
argument_list|(
name|s
argument_list|)
decl_stmt|;
name|Result
index|[]
name|next
init|=
name|scanner
operator|.
name|next
argument_list|(
literal|3
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|next
operator|.
name|length
operator|==
literal|1
argument_list|)
expr_stmt|;
name|CellScanner
name|cellScanner
init|=
name|next
index|[
literal|0
index|]
operator|.
name|cellScanner
argument_list|()
decl_stmt|;
name|cellScanner
operator|.
name|advance
argument_list|()
expr_stmt|;
name|Cell
name|current
init|=
name|cellScanner
operator|.
name|current
argument_list|()
decl_stmt|;
name|assertTrue
argument_list|(
name|Bytes
operator|.
name|equals
argument_list|(
name|current
operator|.
name|getRowArray
argument_list|()
argument_list|,
name|current
operator|.
name|getRowOffset
argument_list|()
argument_list|,
name|current
operator|.
name|getRowLength
argument_list|()
argument_list|,
name|row2
argument_list|,
literal|0
argument_list|,
name|row2
operator|.
name|length
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Test
specifier|public
name|void
name|testVisibilityLabelsWithDeleteFamily
parameter_list|()
throws|throws
name|Exception
block|{
name|setAuths
argument_list|()
expr_stmt|;
specifier|final
name|TableName
name|tableName
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
name|testName
operator|.
name|getMethodName
argument_list|()
argument_list|)
decl_stmt|;
try|try
init|(
name|Table
name|table
init|=
name|createTableAndWriteDataWithLabels
argument_list|(
name|SECRET
argument_list|,
name|CONFIDENTIAL
operator|+
literal|"|"
operator|+
name|TOPSECRET
argument_list|)
init|)
block|{
name|PrivilegedExceptionAction
argument_list|<
name|Void
argument_list|>
name|actiona
init|=
operator|new
name|PrivilegedExceptionAction
argument_list|<
name|Void
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|Void
name|run
parameter_list|()
throws|throws
name|Exception
block|{
try|try
init|(
name|Connection
name|connection
init|=
name|ConnectionFactory
operator|.
name|createConnection
argument_list|(
name|conf
argument_list|)
init|;
name|Table
name|table
operator|=
name|connection
operator|.
name|getTable
argument_list|(
name|tableName
argument_list|)
init|)
block|{
name|Delete
name|d
init|=
operator|new
name|Delete
argument_list|(
name|row2
argument_list|)
decl_stmt|;
name|d
operator|.
name|setCellVisibility
argument_list|(
operator|new
name|CellVisibility
argument_list|(
name|TOPSECRET
operator|+
literal|"|"
operator|+
name|CONFIDENTIAL
argument_list|)
argument_list|)
expr_stmt|;
name|d
operator|.
name|addFamily
argument_list|(
name|fam
argument_list|)
expr_stmt|;
name|table
operator|.
name|delete
argument_list|(
name|d
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Throwable
name|t
parameter_list|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
name|t
argument_list|)
throw|;
block|}
return|return
literal|null
return|;
block|}
block|}
decl_stmt|;
name|SUPERUSER
operator|.
name|runAs
argument_list|(
name|actiona
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|getAdmin
argument_list|()
operator|.
name|flush
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
name|Scan
name|s
init|=
operator|new
name|Scan
argument_list|()
decl_stmt|;
name|s
operator|.
name|setAuthorizations
argument_list|(
operator|new
name|Authorizations
argument_list|(
name|SECRET
argument_list|,
name|PRIVATE
argument_list|,
name|CONFIDENTIAL
argument_list|)
argument_list|)
expr_stmt|;
name|ResultScanner
name|scanner
init|=
name|table
operator|.
name|getScanner
argument_list|(
name|s
argument_list|)
decl_stmt|;
name|Result
index|[]
name|next
init|=
name|scanner
operator|.
name|next
argument_list|(
literal|3
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|next
operator|.
name|length
operator|==
literal|1
argument_list|)
expr_stmt|;
name|CellScanner
name|cellScanner
init|=
name|next
index|[
literal|0
index|]
operator|.
name|cellScanner
argument_list|()
decl_stmt|;
name|cellScanner
operator|.
name|advance
argument_list|()
expr_stmt|;
name|Cell
name|current
init|=
name|cellScanner
operator|.
name|current
argument_list|()
decl_stmt|;
name|assertTrue
argument_list|(
name|Bytes
operator|.
name|equals
argument_list|(
name|current
operator|.
name|getRowArray
argument_list|()
argument_list|,
name|current
operator|.
name|getRowOffset
argument_list|()
argument_list|,
name|current
operator|.
name|getRowLength
argument_list|()
argument_list|,
name|row1
argument_list|,
literal|0
argument_list|,
name|row1
operator|.
name|length
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Test
specifier|public
name|void
name|testVisibilityLabelsWithDeleteFamilyVersion
parameter_list|()
throws|throws
name|Exception
block|{
name|setAuths
argument_list|()
expr_stmt|;
specifier|final
name|TableName
name|tableName
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
name|testName
operator|.
name|getMethodName
argument_list|()
argument_list|)
decl_stmt|;
name|long
index|[]
name|ts
init|=
operator|new
name|long
index|[]
block|{
literal|123L
block|,
literal|125L
block|}
decl_stmt|;
try|try
init|(
name|Table
name|table
init|=
name|createTableAndWriteDataWithLabels
argument_list|(
name|ts
argument_list|,
name|CONFIDENTIAL
operator|+
literal|"|"
operator|+
name|TOPSECRET
argument_list|,
name|SECRET
argument_list|)
init|)
block|{
name|PrivilegedExceptionAction
argument_list|<
name|Void
argument_list|>
name|actiona
init|=
operator|new
name|PrivilegedExceptionAction
argument_list|<
name|Void
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|Void
name|run
parameter_list|()
throws|throws
name|Exception
block|{
try|try
init|(
name|Connection
name|connection
init|=
name|ConnectionFactory
operator|.
name|createConnection
argument_list|(
name|conf
argument_list|)
init|;
name|Table
name|table
operator|=
name|connection
operator|.
name|getTable
argument_list|(
name|tableName
argument_list|)
init|)
block|{
name|Delete
name|d
init|=
operator|new
name|Delete
argument_list|(
name|row1
argument_list|)
decl_stmt|;
name|d
operator|.
name|setCellVisibility
argument_list|(
operator|new
name|CellVisibility
argument_list|(
name|TOPSECRET
operator|+
literal|"|"
operator|+
name|CONFIDENTIAL
argument_list|)
argument_list|)
expr_stmt|;
name|d
operator|.
name|addFamilyVersion
argument_list|(
name|fam
argument_list|,
literal|123L
argument_list|)
expr_stmt|;
name|table
operator|.
name|delete
argument_list|(
name|d
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Throwable
name|t
parameter_list|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
name|t
argument_list|)
throw|;
block|}
return|return
literal|null
return|;
block|}
block|}
decl_stmt|;
name|SUPERUSER
operator|.
name|runAs
argument_list|(
name|actiona
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|getAdmin
argument_list|()
operator|.
name|flush
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
name|Scan
name|s
init|=
operator|new
name|Scan
argument_list|()
decl_stmt|;
name|s
operator|.
name|setAuthorizations
argument_list|(
operator|new
name|Authorizations
argument_list|(
name|SECRET
argument_list|,
name|PRIVATE
argument_list|,
name|CONFIDENTIAL
argument_list|)
argument_list|)
expr_stmt|;
name|ResultScanner
name|scanner
init|=
name|table
operator|.
name|getScanner
argument_list|(
name|s
argument_list|)
decl_stmt|;
name|Result
index|[]
name|next
init|=
name|scanner
operator|.
name|next
argument_list|(
literal|3
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|next
operator|.
name|length
operator|==
literal|1
argument_list|)
expr_stmt|;
name|CellScanner
name|cellScanner
init|=
name|next
index|[
literal|0
index|]
operator|.
name|cellScanner
argument_list|()
decl_stmt|;
name|cellScanner
operator|.
name|advance
argument_list|()
expr_stmt|;
name|Cell
name|current
init|=
name|cellScanner
operator|.
name|current
argument_list|()
decl_stmt|;
name|assertTrue
argument_list|(
name|Bytes
operator|.
name|equals
argument_list|(
name|current
operator|.
name|getRowArray
argument_list|()
argument_list|,
name|current
operator|.
name|getRowOffset
argument_list|()
argument_list|,
name|current
operator|.
name|getRowLength
argument_list|()
argument_list|,
name|row2
argument_list|,
literal|0
argument_list|,
name|row2
operator|.
name|length
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Test
specifier|public
name|void
name|testVisibilityLabelsWithDeleteColumnExactVersion
parameter_list|()
throws|throws
name|Exception
block|{
name|setAuths
argument_list|()
expr_stmt|;
specifier|final
name|TableName
name|tableName
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
name|testName
operator|.
name|getMethodName
argument_list|()
argument_list|)
decl_stmt|;
name|long
index|[]
name|ts
init|=
operator|new
name|long
index|[]
block|{
literal|123L
block|,
literal|125L
block|}
decl_stmt|;
try|try
init|(
name|Table
name|table
init|=
name|createTableAndWriteDataWithLabels
argument_list|(
name|ts
argument_list|,
name|CONFIDENTIAL
operator|+
literal|"|"
operator|+
name|TOPSECRET
argument_list|,
name|SECRET
argument_list|)
init|)
block|{
name|PrivilegedExceptionAction
argument_list|<
name|Void
argument_list|>
name|actiona
init|=
operator|new
name|PrivilegedExceptionAction
argument_list|<
name|Void
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|Void
name|run
parameter_list|()
throws|throws
name|Exception
block|{
try|try
init|(
name|Connection
name|connection
init|=
name|ConnectionFactory
operator|.
name|createConnection
argument_list|(
name|conf
argument_list|)
init|;
name|Table
name|table
operator|=
name|connection
operator|.
name|getTable
argument_list|(
name|tableName
argument_list|)
init|)
block|{
name|Delete
name|d
init|=
operator|new
name|Delete
argument_list|(
name|row1
argument_list|)
decl_stmt|;
name|d
operator|.
name|setCellVisibility
argument_list|(
operator|new
name|CellVisibility
argument_list|(
name|TOPSECRET
operator|+
literal|"|"
operator|+
name|CONFIDENTIAL
argument_list|)
argument_list|)
expr_stmt|;
name|d
operator|.
name|addColumn
argument_list|(
name|fam
argument_list|,
name|qual
argument_list|,
literal|123L
argument_list|)
expr_stmt|;
name|table
operator|.
name|delete
argument_list|(
name|d
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Throwable
name|t
parameter_list|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
name|t
argument_list|)
throw|;
block|}
return|return
literal|null
return|;
block|}
block|}
decl_stmt|;
name|SUPERUSER
operator|.
name|runAs
argument_list|(
name|actiona
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|getAdmin
argument_list|()
operator|.
name|flush
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
name|Scan
name|s
init|=
operator|new
name|Scan
argument_list|()
decl_stmt|;
name|s
operator|.
name|setAuthorizations
argument_list|(
operator|new
name|Authorizations
argument_list|(
name|SECRET
argument_list|,
name|PRIVATE
argument_list|,
name|CONFIDENTIAL
argument_list|)
argument_list|)
expr_stmt|;
name|ResultScanner
name|scanner
init|=
name|table
operator|.
name|getScanner
argument_list|(
name|s
argument_list|)
decl_stmt|;
name|Result
index|[]
name|next
init|=
name|scanner
operator|.
name|next
argument_list|(
literal|3
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|next
operator|.
name|length
operator|==
literal|1
argument_list|)
expr_stmt|;
name|CellScanner
name|cellScanner
init|=
name|next
index|[
literal|0
index|]
operator|.
name|cellScanner
argument_list|()
decl_stmt|;
name|cellScanner
operator|.
name|advance
argument_list|()
expr_stmt|;
name|Cell
name|current
init|=
name|cellScanner
operator|.
name|current
argument_list|()
decl_stmt|;
name|assertTrue
argument_list|(
name|Bytes
operator|.
name|equals
argument_list|(
name|current
operator|.
name|getRowArray
argument_list|()
argument_list|,
name|current
operator|.
name|getRowOffset
argument_list|()
argument_list|,
name|current
operator|.
name|getRowLength
argument_list|()
argument_list|,
name|row2
argument_list|,
literal|0
argument_list|,
name|row2
operator|.
name|length
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

