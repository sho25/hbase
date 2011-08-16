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
name|coprocessor
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
name|lang
operator|.
name|reflect
operator|.
name|Method
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
name|Coprocessor
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
name|HRegionInfo
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
name|MiniHBaseCluster
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
name|hbase
operator|.
name|regionserver
operator|.
name|RegionCoprocessorHost
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
name|JVMClusterUtil
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
name|Test
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
name|TestRegionObserverInterface
block|{
specifier|static
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|TestRegionObserverInterface
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|static
specifier|final
name|String
name|DIR
init|=
literal|"test/build/data/TestRegionObserver/"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|byte
index|[]
name|TEST_TABLE
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"TestTable"
argument_list|)
decl_stmt|;
specifier|public
specifier|final
specifier|static
name|byte
index|[]
name|A
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"a"
argument_list|)
decl_stmt|;
specifier|public
specifier|final
specifier|static
name|byte
index|[]
name|B
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"b"
argument_list|)
decl_stmt|;
specifier|public
specifier|final
specifier|static
name|byte
index|[]
name|C
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"c"
argument_list|)
decl_stmt|;
specifier|public
specifier|final
specifier|static
name|byte
index|[]
name|ROW
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"testrow"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
name|HBaseTestingUtility
name|util
init|=
operator|new
name|HBaseTestingUtility
argument_list|()
decl_stmt|;
specifier|private
specifier|static
name|MiniHBaseCluster
name|cluster
init|=
literal|null
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
comment|// set configure to indicate which cp should be loaded
name|Configuration
name|conf
init|=
name|util
operator|.
name|getConfiguration
argument_list|()
decl_stmt|;
name|conf
operator|.
name|set
argument_list|(
name|CoprocessorHost
operator|.
name|REGION_COPROCESSOR_CONF_KEY
argument_list|,
literal|"org.apache.hadoop.hbase.coprocessor.SimpleRegionObserver"
argument_list|)
expr_stmt|;
name|util
operator|.
name|startMiniCluster
argument_list|(
literal|2
argument_list|)
expr_stmt|;
name|cluster
operator|=
name|util
operator|.
name|getMiniHBaseCluster
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
name|util
operator|.
name|shutdownMiniCluster
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testRegionObserver
parameter_list|()
throws|throws
name|IOException
block|{
name|byte
index|[]
name|tableName
init|=
name|TEST_TABLE
decl_stmt|;
comment|// recreate table every time in order to reset the status of the
comment|// coproccessor.
name|HTable
name|table
init|=
name|util
operator|.
name|createTable
argument_list|(
name|tableName
argument_list|,
operator|new
name|byte
index|[]
index|[]
block|{
name|A
block|,
name|B
block|,
name|C
block|}
argument_list|)
decl_stmt|;
name|verifyMethodResult
argument_list|(
name|SimpleRegionObserver
operator|.
name|class
argument_list|,
operator|new
name|String
index|[]
block|{
literal|"hadPreGet"
block|,
literal|"hadPostGet"
block|,
literal|"hadPrePut"
block|,
literal|"hadPostPut"
block|,
literal|"hadDelete"
block|}
argument_list|,
name|TEST_TABLE
argument_list|,
operator|new
name|Boolean
index|[]
block|{
literal|false
block|,
literal|false
block|,
literal|false
block|,
literal|false
block|,
literal|false
block|}
argument_list|)
expr_stmt|;
name|Put
name|put
init|=
operator|new
name|Put
argument_list|(
name|ROW
argument_list|)
decl_stmt|;
name|put
operator|.
name|add
argument_list|(
name|A
argument_list|,
name|A
argument_list|,
name|A
argument_list|)
expr_stmt|;
name|put
operator|.
name|add
argument_list|(
name|B
argument_list|,
name|B
argument_list|,
name|B
argument_list|)
expr_stmt|;
name|put
operator|.
name|add
argument_list|(
name|C
argument_list|,
name|C
argument_list|,
name|C
argument_list|)
expr_stmt|;
name|table
operator|.
name|put
argument_list|(
name|put
argument_list|)
expr_stmt|;
name|verifyMethodResult
argument_list|(
name|SimpleRegionObserver
operator|.
name|class
argument_list|,
operator|new
name|String
index|[]
block|{
literal|"hadPreGet"
block|,
literal|"hadPostGet"
block|,
literal|"hadPrePut"
block|,
literal|"hadPostPut"
block|,
literal|"hadDelete"
block|}
argument_list|,
name|TEST_TABLE
argument_list|,
operator|new
name|Boolean
index|[]
block|{
literal|false
block|,
literal|false
block|,
literal|true
block|,
literal|true
block|,
literal|false
block|}
argument_list|)
expr_stmt|;
name|Get
name|get
init|=
operator|new
name|Get
argument_list|(
name|ROW
argument_list|)
decl_stmt|;
name|get
operator|.
name|addColumn
argument_list|(
name|A
argument_list|,
name|A
argument_list|)
expr_stmt|;
name|get
operator|.
name|addColumn
argument_list|(
name|B
argument_list|,
name|B
argument_list|)
expr_stmt|;
name|get
operator|.
name|addColumn
argument_list|(
name|C
argument_list|,
name|C
argument_list|)
expr_stmt|;
name|table
operator|.
name|get
argument_list|(
name|get
argument_list|)
expr_stmt|;
name|verifyMethodResult
argument_list|(
name|SimpleRegionObserver
operator|.
name|class
argument_list|,
operator|new
name|String
index|[]
block|{
literal|"hadPreGet"
block|,
literal|"hadPostGet"
block|,
literal|"hadPrePut"
block|,
literal|"hadPostPut"
block|,
literal|"hadDelete"
block|}
argument_list|,
name|TEST_TABLE
argument_list|,
operator|new
name|Boolean
index|[]
block|{
literal|true
block|,
literal|true
block|,
literal|true
block|,
literal|true
block|,
literal|false
block|}
argument_list|)
expr_stmt|;
name|Delete
name|delete
init|=
operator|new
name|Delete
argument_list|(
name|ROW
argument_list|)
decl_stmt|;
name|delete
operator|.
name|deleteColumn
argument_list|(
name|A
argument_list|,
name|A
argument_list|)
expr_stmt|;
name|delete
operator|.
name|deleteColumn
argument_list|(
name|B
argument_list|,
name|B
argument_list|)
expr_stmt|;
name|delete
operator|.
name|deleteColumn
argument_list|(
name|C
argument_list|,
name|C
argument_list|)
expr_stmt|;
name|table
operator|.
name|delete
argument_list|(
name|delete
argument_list|)
expr_stmt|;
name|verifyMethodResult
argument_list|(
name|SimpleRegionObserver
operator|.
name|class
argument_list|,
operator|new
name|String
index|[]
block|{
literal|"hadPreGet"
block|,
literal|"hadPostGet"
block|,
literal|"hadPrePut"
block|,
literal|"hadPostPut"
block|,
literal|"hadDelete"
block|}
argument_list|,
name|TEST_TABLE
argument_list|,
operator|new
name|Boolean
index|[]
block|{
literal|true
block|,
literal|true
block|,
literal|true
block|,
literal|true
block|,
literal|true
block|}
argument_list|)
expr_stmt|;
name|util
operator|.
name|deleteTable
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testIncrementHook
parameter_list|()
throws|throws
name|IOException
block|{
name|byte
index|[]
name|tableName
init|=
name|TEST_TABLE
decl_stmt|;
name|HTable
name|table
init|=
name|util
operator|.
name|createTable
argument_list|(
name|tableName
argument_list|,
operator|new
name|byte
index|[]
index|[]
block|{
name|A
block|,
name|B
block|,
name|C
block|}
argument_list|)
decl_stmt|;
name|Increment
name|inc
init|=
operator|new
name|Increment
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|0
argument_list|)
argument_list|)
decl_stmt|;
name|inc
operator|.
name|addColumn
argument_list|(
name|A
argument_list|,
name|A
argument_list|,
literal|1
argument_list|)
expr_stmt|;
name|verifyMethodResult
argument_list|(
name|SimpleRegionObserver
operator|.
name|class
argument_list|,
operator|new
name|String
index|[]
block|{
literal|"hadPreIncrement"
block|,
literal|"hadPostIncrement"
block|}
argument_list|,
name|tableName
argument_list|,
operator|new
name|Boolean
index|[]
block|{
literal|false
block|,
literal|false
block|}
argument_list|)
expr_stmt|;
name|table
operator|.
name|increment
argument_list|(
name|inc
argument_list|)
expr_stmt|;
name|verifyMethodResult
argument_list|(
name|SimpleRegionObserver
operator|.
name|class
argument_list|,
operator|new
name|String
index|[]
block|{
literal|"hadPreIncrement"
block|,
literal|"hadPostIncrement"
block|}
argument_list|,
name|tableName
argument_list|,
operator|new
name|Boolean
index|[]
block|{
literal|true
block|,
literal|true
block|}
argument_list|)
expr_stmt|;
name|util
operator|.
name|deleteTable
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
comment|// HBase-3583
specifier|public
name|void
name|testHBase3583
parameter_list|()
throws|throws
name|IOException
block|{
name|byte
index|[]
name|tableName
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"testHBase3583"
argument_list|)
decl_stmt|;
name|util
operator|.
name|createTable
argument_list|(
name|tableName
argument_list|,
operator|new
name|byte
index|[]
index|[]
block|{
name|A
block|,
name|B
block|,
name|C
block|}
argument_list|)
expr_stmt|;
name|verifyMethodResult
argument_list|(
name|SimpleRegionObserver
operator|.
name|class
argument_list|,
operator|new
name|String
index|[]
block|{
literal|"hadPreGet"
block|,
literal|"hadPostGet"
block|,
literal|"wasScannerNextCalled"
block|,
literal|"wasScannerCloseCalled"
block|}
argument_list|,
name|tableName
argument_list|,
operator|new
name|Boolean
index|[]
block|{
literal|false
block|,
literal|false
block|,
literal|false
block|,
literal|false
block|}
argument_list|)
expr_stmt|;
name|HTable
name|table
init|=
operator|new
name|HTable
argument_list|(
name|util
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|tableName
argument_list|)
decl_stmt|;
name|Put
name|put
init|=
operator|new
name|Put
argument_list|(
name|ROW
argument_list|)
decl_stmt|;
name|put
operator|.
name|add
argument_list|(
name|A
argument_list|,
name|A
argument_list|,
name|A
argument_list|)
expr_stmt|;
name|table
operator|.
name|put
argument_list|(
name|put
argument_list|)
expr_stmt|;
name|Get
name|get
init|=
operator|new
name|Get
argument_list|(
name|ROW
argument_list|)
decl_stmt|;
name|get
operator|.
name|addColumn
argument_list|(
name|A
argument_list|,
name|A
argument_list|)
expr_stmt|;
name|table
operator|.
name|get
argument_list|(
name|get
argument_list|)
expr_stmt|;
comment|// verify that scannerNext and scannerClose upcalls won't be invoked
comment|// when we perform get().
name|verifyMethodResult
argument_list|(
name|SimpleRegionObserver
operator|.
name|class
argument_list|,
operator|new
name|String
index|[]
block|{
literal|"hadPreGet"
block|,
literal|"hadPostGet"
block|,
literal|"wasScannerNextCalled"
block|,
literal|"wasScannerCloseCalled"
block|}
argument_list|,
name|tableName
argument_list|,
operator|new
name|Boolean
index|[]
block|{
literal|true
block|,
literal|true
block|,
literal|false
block|,
literal|false
block|}
argument_list|)
expr_stmt|;
name|Scan
name|s
init|=
operator|new
name|Scan
argument_list|()
decl_stmt|;
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
try|try
block|{
for|for
control|(
name|Result
name|rr
init|=
name|scanner
operator|.
name|next
argument_list|()
init|;
name|rr
operator|!=
literal|null
condition|;
name|rr
operator|=
name|scanner
operator|.
name|next
argument_list|()
control|)
block|{       }
block|}
finally|finally
block|{
name|scanner
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
comment|// now scanner hooks should be invoked.
name|verifyMethodResult
argument_list|(
name|SimpleRegionObserver
operator|.
name|class
argument_list|,
operator|new
name|String
index|[]
block|{
literal|"wasScannerNextCalled"
block|,
literal|"wasScannerCloseCalled"
block|}
argument_list|,
name|tableName
argument_list|,
operator|new
name|Boolean
index|[]
block|{
literal|true
block|,
literal|true
block|}
argument_list|)
expr_stmt|;
name|util
operator|.
name|deleteTable
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
comment|// HBase-3758
specifier|public
name|void
name|testHBase3758
parameter_list|()
throws|throws
name|IOException
block|{
name|byte
index|[]
name|tableName
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"testHBase3758"
argument_list|)
decl_stmt|;
name|util
operator|.
name|createTable
argument_list|(
name|tableName
argument_list|,
operator|new
name|byte
index|[]
index|[]
block|{
name|A
block|,
name|B
block|,
name|C
block|}
argument_list|)
expr_stmt|;
name|verifyMethodResult
argument_list|(
name|SimpleRegionObserver
operator|.
name|class
argument_list|,
operator|new
name|String
index|[]
block|{
literal|"hadDeleted"
block|,
literal|"wasScannerOpenCalled"
block|}
argument_list|,
name|tableName
argument_list|,
operator|new
name|Boolean
index|[]
block|{
literal|false
block|,
literal|false
block|}
argument_list|)
expr_stmt|;
name|HTable
name|table
init|=
operator|new
name|HTable
argument_list|(
name|util
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|tableName
argument_list|)
decl_stmt|;
name|Put
name|put
init|=
operator|new
name|Put
argument_list|(
name|ROW
argument_list|)
decl_stmt|;
name|put
operator|.
name|add
argument_list|(
name|A
argument_list|,
name|A
argument_list|,
name|A
argument_list|)
expr_stmt|;
name|table
operator|.
name|put
argument_list|(
name|put
argument_list|)
expr_stmt|;
name|Delete
name|delete
init|=
operator|new
name|Delete
argument_list|(
name|ROW
argument_list|)
decl_stmt|;
name|table
operator|.
name|delete
argument_list|(
name|delete
argument_list|)
expr_stmt|;
name|verifyMethodResult
argument_list|(
name|SimpleRegionObserver
operator|.
name|class
argument_list|,
operator|new
name|String
index|[]
block|{
literal|"hadDeleted"
block|,
literal|"wasScannerOpenCalled"
block|}
argument_list|,
name|tableName
argument_list|,
operator|new
name|Boolean
index|[]
block|{
literal|true
block|,
literal|false
block|}
argument_list|)
expr_stmt|;
name|Scan
name|s
init|=
operator|new
name|Scan
argument_list|()
decl_stmt|;
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
try|try
block|{
for|for
control|(
name|Result
name|rr
init|=
name|scanner
operator|.
name|next
argument_list|()
init|;
name|rr
operator|!=
literal|null
condition|;
name|rr
operator|=
name|scanner
operator|.
name|next
argument_list|()
control|)
block|{       }
block|}
finally|finally
block|{
name|scanner
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
comment|// now scanner hooks should be invoked.
name|verifyMethodResult
argument_list|(
name|SimpleRegionObserver
operator|.
name|class
argument_list|,
operator|new
name|String
index|[]
block|{
literal|"wasScannerOpenCalled"
block|}
argument_list|,
name|tableName
argument_list|,
operator|new
name|Boolean
index|[]
block|{
literal|true
block|}
argument_list|)
expr_stmt|;
name|util
operator|.
name|deleteTable
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
block|}
comment|// check each region whether the coprocessor upcalls are called or not.
specifier|private
name|void
name|verifyMethodResult
parameter_list|(
name|Class
name|c
parameter_list|,
name|String
name|methodName
index|[]
parameter_list|,
name|byte
index|[]
name|tableName
parameter_list|,
name|Object
name|value
index|[]
parameter_list|)
throws|throws
name|IOException
block|{
try|try
block|{
for|for
control|(
name|JVMClusterUtil
operator|.
name|RegionServerThread
name|t
range|:
name|cluster
operator|.
name|getRegionServerThreads
argument_list|()
control|)
block|{
for|for
control|(
name|HRegionInfo
name|r
range|:
name|t
operator|.
name|getRegionServer
argument_list|()
operator|.
name|getOnlineRegions
argument_list|()
control|)
block|{
if|if
condition|(
operator|!
name|Arrays
operator|.
name|equals
argument_list|(
name|r
operator|.
name|getTableName
argument_list|()
argument_list|,
name|tableName
argument_list|)
condition|)
block|{
continue|continue;
block|}
name|RegionCoprocessorHost
name|cph
init|=
name|t
operator|.
name|getRegionServer
argument_list|()
operator|.
name|getOnlineRegion
argument_list|(
name|r
operator|.
name|getRegionName
argument_list|()
argument_list|)
operator|.
name|getCoprocessorHost
argument_list|()
decl_stmt|;
name|Coprocessor
name|cp
init|=
name|cph
operator|.
name|findCoprocessor
argument_list|(
name|c
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
name|assertNotNull
argument_list|(
name|cp
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|methodName
operator|.
name|length
condition|;
operator|++
name|i
control|)
block|{
name|Method
name|m
init|=
name|c
operator|.
name|getMethod
argument_list|(
name|methodName
index|[
name|i
index|]
argument_list|)
decl_stmt|;
name|Object
name|o
init|=
name|m
operator|.
name|invoke
argument_list|(
name|cp
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
literal|"Result of "
operator|+
name|c
operator|.
name|getName
argument_list|()
operator|+
literal|"."
operator|+
name|methodName
index|[
name|i
index|]
operator|+
literal|" is expected to be "
operator|+
name|value
index|[
name|i
index|]
operator|.
name|toString
argument_list|()
operator|+
literal|", while we get "
operator|+
name|o
operator|.
name|toString
argument_list|()
argument_list|,
name|o
operator|.
name|equals
argument_list|(
name|value
index|[
name|i
index|]
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
name|e
operator|.
name|toString
argument_list|()
argument_list|)
throw|;
block|}
block|}
specifier|private
specifier|static
name|byte
index|[]
index|[]
name|makeN
parameter_list|(
name|byte
index|[]
name|base
parameter_list|,
name|int
name|n
parameter_list|)
block|{
name|byte
index|[]
index|[]
name|ret
init|=
operator|new
name|byte
index|[
name|n
index|]
index|[]
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
name|n
condition|;
name|i
operator|++
control|)
block|{
name|ret
index|[
name|i
index|]
operator|=
name|Bytes
operator|.
name|add
argument_list|(
name|base
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
name|String
operator|.
name|format
argument_list|(
literal|"%02d"
argument_list|,
name|i
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|ret
return|;
block|}
block|}
end_class

end_unit

