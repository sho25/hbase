begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Copyright 2011 The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|regionserver
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
name|List
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
name|client
operator|.
name|Get
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
name|Row
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
name|coprocessor
operator|.
name|Batch
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
name|coprocessor
operator|.
name|CoprocessorHost
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
name|ipc
operator|.
name|CoprocessorProtocol
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
name|ipc
operator|.
name|HMasterInterface
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
name|ipc
operator|.
name|HMasterRegionInterface
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
name|ipc
operator|.
name|ProtocolSignature
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
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|ipc
operator|.
name|VersionedProtocol
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
import|import
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|Lists
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
name|MediumTests
operator|.
name|class
argument_list|)
specifier|public
class|class
name|TestServerCustomProtocol
block|{
comment|/* Test protocol */
specifier|public
specifier|static
interface|interface
name|PingProtocol
extends|extends
name|CoprocessorProtocol
block|{
specifier|public
name|String
name|ping
parameter_list|()
function_decl|;
specifier|public
name|int
name|getPingCount
parameter_list|()
function_decl|;
specifier|public
name|int
name|incrementCount
parameter_list|(
name|int
name|diff
parameter_list|)
function_decl|;
specifier|public
name|String
name|hello
parameter_list|(
name|String
name|name
parameter_list|)
function_decl|;
specifier|public
name|void
name|noop
parameter_list|()
function_decl|;
block|}
comment|/* Test protocol implementation */
specifier|public
specifier|static
class|class
name|PingHandler
implements|implements
name|Coprocessor
implements|,
name|PingProtocol
implements|,
name|VersionedProtocol
block|{
specifier|static
name|long
name|VERSION
init|=
literal|1
decl_stmt|;
specifier|private
name|int
name|counter
init|=
literal|0
decl_stmt|;
annotation|@
name|Override
specifier|public
name|String
name|ping
parameter_list|()
block|{
name|counter
operator|++
expr_stmt|;
return|return
literal|"pong"
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|getPingCount
parameter_list|()
block|{
return|return
name|counter
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|incrementCount
parameter_list|(
name|int
name|diff
parameter_list|)
block|{
name|counter
operator|+=
name|diff
expr_stmt|;
return|return
name|counter
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|hello
parameter_list|(
name|String
name|name
parameter_list|)
block|{
if|if
condition|(
name|name
operator|==
literal|null
condition|)
block|{
return|return
literal|"Who are you?"
return|;
block|}
elseif|else
if|if
condition|(
literal|"nobody"
operator|.
name|equals
argument_list|(
name|name
argument_list|)
condition|)
block|{
return|return
literal|null
return|;
block|}
return|return
literal|"Hello, "
operator|+
name|name
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|noop
parameter_list|()
block|{
comment|// do nothing, just test void return type
block|}
annotation|@
name|Override
specifier|public
name|ProtocolSignature
name|getProtocolSignature
parameter_list|(
name|String
name|protocol
parameter_list|,
name|long
name|version
parameter_list|,
name|int
name|clientMethodsHashCode
parameter_list|)
throws|throws
name|IOException
block|{
return|return
operator|new
name|ProtocolSignature
argument_list|(
name|VERSION
argument_list|,
literal|null
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|getProtocolVersion
parameter_list|(
name|String
name|s
parameter_list|,
name|long
name|l
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|VERSION
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|start
parameter_list|(
name|CoprocessorEnvironment
name|env
parameter_list|)
throws|throws
name|IOException
block|{     }
annotation|@
name|Override
specifier|public
name|void
name|stop
parameter_list|(
name|CoprocessorEnvironment
name|env
parameter_list|)
throws|throws
name|IOException
block|{     }
block|}
specifier|private
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
literal|"test"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|TEST_FAMILY
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"f1"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|ROW_A
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"aaa"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|ROW_B
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"bbb"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|ROW_C
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"ccc"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|ROW_AB
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"abb"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|ROW_BC
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"bcc"
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
name|util
operator|.
name|getConfiguration
argument_list|()
operator|.
name|set
argument_list|(
name|CoprocessorHost
operator|.
name|REGION_COPROCESSOR_CONF_KEY
argument_list|,
name|PingHandler
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|util
operator|.
name|startMiniCluster
argument_list|(
literal|1
argument_list|)
expr_stmt|;
name|cluster
operator|=
name|util
operator|.
name|getMiniHBaseCluster
argument_list|()
expr_stmt|;
name|HTable
name|table
init|=
name|util
operator|.
name|createTable
argument_list|(
name|TEST_TABLE
argument_list|,
name|TEST_FAMILY
argument_list|)
decl_stmt|;
name|util
operator|.
name|createMultiRegions
argument_list|(
name|util
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|table
argument_list|,
name|TEST_FAMILY
argument_list|,
operator|new
name|byte
index|[]
index|[]
block|{
name|HConstants
operator|.
name|EMPTY_BYTE_ARRAY
block|,
name|ROW_B
block|,
name|ROW_C
block|}
argument_list|)
expr_stmt|;
name|Put
name|puta
init|=
operator|new
name|Put
argument_list|(
name|ROW_A
argument_list|)
decl_stmt|;
name|puta
operator|.
name|add
argument_list|(
name|TEST_FAMILY
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"col1"
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|table
operator|.
name|put
argument_list|(
name|puta
argument_list|)
expr_stmt|;
name|Put
name|putb
init|=
operator|new
name|Put
argument_list|(
name|ROW_B
argument_list|)
decl_stmt|;
name|putb
operator|.
name|add
argument_list|(
name|TEST_FAMILY
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"col1"
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|table
operator|.
name|put
argument_list|(
name|putb
argument_list|)
expr_stmt|;
name|Put
name|putc
init|=
operator|new
name|Put
argument_list|(
name|ROW_C
argument_list|)
decl_stmt|;
name|putc
operator|.
name|add
argument_list|(
name|TEST_FAMILY
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"col1"
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|table
operator|.
name|put
argument_list|(
name|putc
argument_list|)
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
name|testSingleProxy
parameter_list|()
throws|throws
name|Exception
block|{
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
name|TEST_TABLE
argument_list|)
decl_stmt|;
name|PingProtocol
name|pinger
init|=
name|table
operator|.
name|coprocessorProxy
argument_list|(
name|PingProtocol
operator|.
name|class
argument_list|,
name|ROW_A
argument_list|)
decl_stmt|;
name|String
name|result
init|=
name|pinger
operator|.
name|ping
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
literal|"Invalid custom protocol response"
argument_list|,
literal|"pong"
argument_list|,
name|result
argument_list|)
expr_stmt|;
name|result
operator|=
name|pinger
operator|.
name|hello
argument_list|(
literal|"George"
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"Invalid custom protocol response"
argument_list|,
literal|"Hello, George"
argument_list|,
name|result
argument_list|)
expr_stmt|;
name|result
operator|=
name|pinger
operator|.
name|hello
argument_list|(
literal|null
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"Should handle NULL parameter"
argument_list|,
literal|"Who are you?"
argument_list|,
name|result
argument_list|)
expr_stmt|;
name|result
operator|=
name|pinger
operator|.
name|hello
argument_list|(
literal|"nobody"
argument_list|)
expr_stmt|;
name|assertNull
argument_list|(
name|result
argument_list|)
expr_stmt|;
name|int
name|cnt
init|=
name|pinger
operator|.
name|getPingCount
argument_list|()
decl_stmt|;
name|assertTrue
argument_list|(
literal|"Count should be incremented"
argument_list|,
name|cnt
operator|>
literal|0
argument_list|)
expr_stmt|;
name|int
name|newcnt
init|=
name|pinger
operator|.
name|incrementCount
argument_list|(
literal|5
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|"Counter should have incremented by 5"
argument_list|,
name|cnt
operator|+
literal|5
argument_list|,
name|newcnt
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testSingleMethod
parameter_list|()
throws|throws
name|Throwable
block|{
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
name|TEST_TABLE
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|?
extends|extends
name|Row
argument_list|>
name|rows
init|=
name|Lists
operator|.
name|newArrayList
argument_list|(
operator|new
name|Get
argument_list|(
name|ROW_A
argument_list|)
argument_list|,
operator|new
name|Get
argument_list|(
name|ROW_B
argument_list|)
argument_list|,
operator|new
name|Get
argument_list|(
name|ROW_C
argument_list|)
argument_list|)
decl_stmt|;
name|Batch
operator|.
name|Call
argument_list|<
name|PingProtocol
argument_list|,
name|String
argument_list|>
name|call
init|=
name|Batch
operator|.
name|forMethod
argument_list|(
name|PingProtocol
operator|.
name|class
argument_list|,
literal|"ping"
argument_list|)
decl_stmt|;
name|Map
argument_list|<
name|byte
index|[]
argument_list|,
name|String
argument_list|>
name|results
init|=
name|table
operator|.
name|coprocessorExec
argument_list|(
name|PingProtocol
operator|.
name|class
argument_list|,
name|ROW_A
argument_list|,
name|ROW_C
argument_list|,
name|call
argument_list|)
decl_stmt|;
name|verifyRegionResults
argument_list|(
name|table
argument_list|,
name|results
argument_list|,
name|ROW_A
argument_list|)
expr_stmt|;
name|verifyRegionResults
argument_list|(
name|table
argument_list|,
name|results
argument_list|,
name|ROW_B
argument_list|)
expr_stmt|;
name|verifyRegionResults
argument_list|(
name|table
argument_list|,
name|results
argument_list|,
name|ROW_C
argument_list|)
expr_stmt|;
name|Batch
operator|.
name|Call
argument_list|<
name|PingProtocol
argument_list|,
name|String
argument_list|>
name|helloCall
init|=
name|Batch
operator|.
name|forMethod
argument_list|(
name|PingProtocol
operator|.
name|class
argument_list|,
literal|"hello"
argument_list|,
literal|"NAME"
argument_list|)
decl_stmt|;
name|results
operator|=
name|table
operator|.
name|coprocessorExec
argument_list|(
name|PingProtocol
operator|.
name|class
argument_list|,
name|ROW_A
argument_list|,
name|ROW_C
argument_list|,
name|helloCall
argument_list|)
expr_stmt|;
name|verifyRegionResults
argument_list|(
name|table
argument_list|,
name|results
argument_list|,
literal|"Hello, NAME"
argument_list|,
name|ROW_A
argument_list|)
expr_stmt|;
name|verifyRegionResults
argument_list|(
name|table
argument_list|,
name|results
argument_list|,
literal|"Hello, NAME"
argument_list|,
name|ROW_B
argument_list|)
expr_stmt|;
name|verifyRegionResults
argument_list|(
name|table
argument_list|,
name|results
argument_list|,
literal|"Hello, NAME"
argument_list|,
name|ROW_C
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testRowRange
parameter_list|()
throws|throws
name|Throwable
block|{
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
name|TEST_TABLE
argument_list|)
decl_stmt|;
comment|// test empty range
name|Map
argument_list|<
name|byte
index|[]
argument_list|,
name|String
argument_list|>
name|results
init|=
name|table
operator|.
name|coprocessorExec
argument_list|(
name|PingProtocol
operator|.
name|class
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
operator|new
name|Batch
operator|.
name|Call
argument_list|<
name|PingProtocol
argument_list|,
name|String
argument_list|>
argument_list|()
block|{
specifier|public
name|String
name|call
parameter_list|(
name|PingProtocol
name|instance
parameter_list|)
block|{
return|return
name|instance
operator|.
name|ping
argument_list|()
return|;
block|}
block|}
argument_list|)
decl_stmt|;
comment|// should contain all three rows/regions
name|verifyRegionResults
argument_list|(
name|table
argument_list|,
name|results
argument_list|,
name|ROW_A
argument_list|)
expr_stmt|;
name|verifyRegionResults
argument_list|(
name|table
argument_list|,
name|results
argument_list|,
name|ROW_B
argument_list|)
expr_stmt|;
name|verifyRegionResults
argument_list|(
name|table
argument_list|,
name|results
argument_list|,
name|ROW_C
argument_list|)
expr_stmt|;
comment|// test start row + empty end
name|results
operator|=
name|table
operator|.
name|coprocessorExec
argument_list|(
name|PingProtocol
operator|.
name|class
argument_list|,
name|ROW_BC
argument_list|,
literal|null
argument_list|,
operator|new
name|Batch
operator|.
name|Call
argument_list|<
name|PingProtocol
argument_list|,
name|String
argument_list|>
argument_list|()
block|{
specifier|public
name|String
name|call
parameter_list|(
name|PingProtocol
name|instance
parameter_list|)
block|{
return|return
name|instance
operator|.
name|ping
argument_list|()
return|;
block|}
block|}
argument_list|)
expr_stmt|;
comment|// should contain last 2 regions
name|HRegionLocation
name|loc
init|=
name|table
operator|.
name|getRegionLocation
argument_list|(
name|ROW_A
argument_list|)
decl_stmt|;
name|assertNull
argument_list|(
literal|"Should be missing region for row aaa (prior to start row)"
argument_list|,
name|results
operator|.
name|get
argument_list|(
name|loc
operator|.
name|getRegionInfo
argument_list|()
operator|.
name|getRegionName
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|verifyRegionResults
argument_list|(
name|table
argument_list|,
name|results
argument_list|,
name|ROW_B
argument_list|)
expr_stmt|;
name|verifyRegionResults
argument_list|(
name|table
argument_list|,
name|results
argument_list|,
name|ROW_C
argument_list|)
expr_stmt|;
comment|// test empty start + end
name|results
operator|=
name|table
operator|.
name|coprocessorExec
argument_list|(
name|PingProtocol
operator|.
name|class
argument_list|,
literal|null
argument_list|,
name|ROW_BC
argument_list|,
operator|new
name|Batch
operator|.
name|Call
argument_list|<
name|PingProtocol
argument_list|,
name|String
argument_list|>
argument_list|()
block|{
specifier|public
name|String
name|call
parameter_list|(
name|PingProtocol
name|instance
parameter_list|)
block|{
return|return
name|instance
operator|.
name|ping
argument_list|()
return|;
block|}
block|}
argument_list|)
expr_stmt|;
comment|// should contain the first 2 regions
name|verifyRegionResults
argument_list|(
name|table
argument_list|,
name|results
argument_list|,
name|ROW_A
argument_list|)
expr_stmt|;
name|verifyRegionResults
argument_list|(
name|table
argument_list|,
name|results
argument_list|,
name|ROW_B
argument_list|)
expr_stmt|;
name|loc
operator|=
name|table
operator|.
name|getRegionLocation
argument_list|(
name|ROW_C
argument_list|)
expr_stmt|;
name|assertNull
argument_list|(
literal|"Should be missing region for row ccc (past stop row)"
argument_list|,
name|results
operator|.
name|get
argument_list|(
name|loc
operator|.
name|getRegionInfo
argument_list|()
operator|.
name|getRegionName
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
comment|// test explicit start + end
name|results
operator|=
name|table
operator|.
name|coprocessorExec
argument_list|(
name|PingProtocol
operator|.
name|class
argument_list|,
name|ROW_AB
argument_list|,
name|ROW_BC
argument_list|,
operator|new
name|Batch
operator|.
name|Call
argument_list|<
name|PingProtocol
argument_list|,
name|String
argument_list|>
argument_list|()
block|{
specifier|public
name|String
name|call
parameter_list|(
name|PingProtocol
name|instance
parameter_list|)
block|{
return|return
name|instance
operator|.
name|ping
argument_list|()
return|;
block|}
block|}
argument_list|)
expr_stmt|;
comment|// should contain first 2 regions
name|verifyRegionResults
argument_list|(
name|table
argument_list|,
name|results
argument_list|,
name|ROW_A
argument_list|)
expr_stmt|;
name|verifyRegionResults
argument_list|(
name|table
argument_list|,
name|results
argument_list|,
name|ROW_B
argument_list|)
expr_stmt|;
name|loc
operator|=
name|table
operator|.
name|getRegionLocation
argument_list|(
name|ROW_C
argument_list|)
expr_stmt|;
name|assertNull
argument_list|(
literal|"Should be missing region for row ccc (past stop row)"
argument_list|,
name|results
operator|.
name|get
argument_list|(
name|loc
operator|.
name|getRegionInfo
argument_list|()
operator|.
name|getRegionName
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
comment|// test single region
name|results
operator|=
name|table
operator|.
name|coprocessorExec
argument_list|(
name|PingProtocol
operator|.
name|class
argument_list|,
name|ROW_B
argument_list|,
name|ROW_BC
argument_list|,
operator|new
name|Batch
operator|.
name|Call
argument_list|<
name|PingProtocol
argument_list|,
name|String
argument_list|>
argument_list|()
block|{
specifier|public
name|String
name|call
parameter_list|(
name|PingProtocol
name|instance
parameter_list|)
block|{
return|return
name|instance
operator|.
name|ping
argument_list|()
return|;
block|}
block|}
argument_list|)
expr_stmt|;
comment|// should only contain region bbb
name|verifyRegionResults
argument_list|(
name|table
argument_list|,
name|results
argument_list|,
name|ROW_B
argument_list|)
expr_stmt|;
name|loc
operator|=
name|table
operator|.
name|getRegionLocation
argument_list|(
name|ROW_A
argument_list|)
expr_stmt|;
name|assertNull
argument_list|(
literal|"Should be missing region for row aaa (prior to start)"
argument_list|,
name|results
operator|.
name|get
argument_list|(
name|loc
operator|.
name|getRegionInfo
argument_list|()
operator|.
name|getRegionName
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|loc
operator|=
name|table
operator|.
name|getRegionLocation
argument_list|(
name|ROW_C
argument_list|)
expr_stmt|;
name|assertNull
argument_list|(
literal|"Should be missing region for row ccc (past stop row)"
argument_list|,
name|results
operator|.
name|get
argument_list|(
name|loc
operator|.
name|getRegionInfo
argument_list|()
operator|.
name|getRegionName
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testCompountCall
parameter_list|()
throws|throws
name|Throwable
block|{
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
name|TEST_TABLE
argument_list|)
decl_stmt|;
name|Map
argument_list|<
name|byte
index|[]
argument_list|,
name|String
argument_list|>
name|results
init|=
name|table
operator|.
name|coprocessorExec
argument_list|(
name|PingProtocol
operator|.
name|class
argument_list|,
name|ROW_A
argument_list|,
name|ROW_C
argument_list|,
operator|new
name|Batch
operator|.
name|Call
argument_list|<
name|PingProtocol
argument_list|,
name|String
argument_list|>
argument_list|()
block|{
specifier|public
name|String
name|call
parameter_list|(
name|PingProtocol
name|instance
parameter_list|)
block|{
return|return
name|instance
operator|.
name|hello
argument_list|(
name|instance
operator|.
name|ping
argument_list|()
argument_list|)
return|;
block|}
block|}
argument_list|)
decl_stmt|;
name|verifyRegionResults
argument_list|(
name|table
argument_list|,
name|results
argument_list|,
literal|"Hello, pong"
argument_list|,
name|ROW_A
argument_list|)
expr_stmt|;
name|verifyRegionResults
argument_list|(
name|table
argument_list|,
name|results
argument_list|,
literal|"Hello, pong"
argument_list|,
name|ROW_B
argument_list|)
expr_stmt|;
name|verifyRegionResults
argument_list|(
name|table
argument_list|,
name|results
argument_list|,
literal|"Hello, pong"
argument_list|,
name|ROW_C
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testNullCall
parameter_list|()
throws|throws
name|Throwable
block|{
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
name|TEST_TABLE
argument_list|)
decl_stmt|;
name|Map
argument_list|<
name|byte
index|[]
argument_list|,
name|String
argument_list|>
name|results
init|=
name|table
operator|.
name|coprocessorExec
argument_list|(
name|PingProtocol
operator|.
name|class
argument_list|,
name|ROW_A
argument_list|,
name|ROW_C
argument_list|,
operator|new
name|Batch
operator|.
name|Call
argument_list|<
name|PingProtocol
argument_list|,
name|String
argument_list|>
argument_list|()
block|{
specifier|public
name|String
name|call
parameter_list|(
name|PingProtocol
name|instance
parameter_list|)
block|{
return|return
name|instance
operator|.
name|hello
argument_list|(
literal|null
argument_list|)
return|;
block|}
block|}
argument_list|)
decl_stmt|;
name|verifyRegionResults
argument_list|(
name|table
argument_list|,
name|results
argument_list|,
literal|"Who are you?"
argument_list|,
name|ROW_A
argument_list|)
expr_stmt|;
name|verifyRegionResults
argument_list|(
name|table
argument_list|,
name|results
argument_list|,
literal|"Who are you?"
argument_list|,
name|ROW_B
argument_list|)
expr_stmt|;
name|verifyRegionResults
argument_list|(
name|table
argument_list|,
name|results
argument_list|,
literal|"Who are you?"
argument_list|,
name|ROW_C
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testNullReturn
parameter_list|()
throws|throws
name|Throwable
block|{
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
name|TEST_TABLE
argument_list|)
decl_stmt|;
name|Map
argument_list|<
name|byte
index|[]
argument_list|,
name|String
argument_list|>
name|results
init|=
name|table
operator|.
name|coprocessorExec
argument_list|(
name|PingProtocol
operator|.
name|class
argument_list|,
name|ROW_A
argument_list|,
name|ROW_C
argument_list|,
operator|new
name|Batch
operator|.
name|Call
argument_list|<
name|PingProtocol
argument_list|,
name|String
argument_list|>
argument_list|()
block|{
specifier|public
name|String
name|call
parameter_list|(
name|PingProtocol
name|instance
parameter_list|)
block|{
return|return
name|instance
operator|.
name|hello
argument_list|(
literal|"nobody"
argument_list|)
return|;
block|}
block|}
argument_list|)
decl_stmt|;
name|verifyRegionResults
argument_list|(
name|table
argument_list|,
name|results
argument_list|,
literal|null
argument_list|,
name|ROW_A
argument_list|)
expr_stmt|;
name|verifyRegionResults
argument_list|(
name|table
argument_list|,
name|results
argument_list|,
literal|null
argument_list|,
name|ROW_B
argument_list|)
expr_stmt|;
name|verifyRegionResults
argument_list|(
name|table
argument_list|,
name|results
argument_list|,
literal|null
argument_list|,
name|ROW_C
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testVoidReturnType
parameter_list|()
throws|throws
name|Throwable
block|{
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
name|TEST_TABLE
argument_list|)
decl_stmt|;
name|Map
argument_list|<
name|byte
index|[]
argument_list|,
name|Object
argument_list|>
name|results
init|=
name|table
operator|.
name|coprocessorExec
argument_list|(
name|PingProtocol
operator|.
name|class
argument_list|,
name|ROW_A
argument_list|,
name|ROW_C
argument_list|,
operator|new
name|Batch
operator|.
name|Call
argument_list|<
name|PingProtocol
argument_list|,
name|Object
argument_list|>
argument_list|()
block|{
specifier|public
name|Object
name|call
parameter_list|(
name|PingProtocol
name|instance
parameter_list|)
block|{
name|instance
operator|.
name|noop
argument_list|()
expr_stmt|;
return|return
literal|null
return|;
block|}
block|}
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|"Should have results from three regions"
argument_list|,
literal|3
argument_list|,
name|results
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
comment|// all results should be null
for|for
control|(
name|Object
name|v
range|:
name|results
operator|.
name|values
argument_list|()
control|)
block|{
name|assertNull
argument_list|(
name|v
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
name|void
name|verifyRegionResults
parameter_list|(
name|HTable
name|table
parameter_list|,
name|Map
argument_list|<
name|byte
index|[]
argument_list|,
name|String
argument_list|>
name|results
parameter_list|,
name|byte
index|[]
name|row
parameter_list|)
throws|throws
name|Exception
block|{
name|verifyRegionResults
argument_list|(
name|table
argument_list|,
name|results
argument_list|,
literal|"pong"
argument_list|,
name|row
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|verifyRegionResults
parameter_list|(
name|HTable
name|table
parameter_list|,
name|Map
argument_list|<
name|byte
index|[]
argument_list|,
name|String
argument_list|>
name|results
parameter_list|,
name|String
name|expected
parameter_list|,
name|byte
index|[]
name|row
parameter_list|)
throws|throws
name|Exception
block|{
name|HRegionLocation
name|loc
init|=
name|table
operator|.
name|getRegionLocation
argument_list|(
name|row
argument_list|)
decl_stmt|;
name|byte
index|[]
name|region
init|=
name|loc
operator|.
name|getRegionInfo
argument_list|()
operator|.
name|getRegionName
argument_list|()
decl_stmt|;
name|assertTrue
argument_list|(
literal|"Results should contain region "
operator|+
name|Bytes
operator|.
name|toStringBinary
argument_list|(
name|region
argument_list|)
operator|+
literal|" for row '"
operator|+
name|Bytes
operator|.
name|toStringBinary
argument_list|(
name|row
argument_list|)
operator|+
literal|"'"
argument_list|,
name|results
operator|.
name|containsKey
argument_list|(
name|region
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"Invalid result for row '"
operator|+
name|Bytes
operator|.
name|toStringBinary
argument_list|(
name|row
argument_list|)
operator|+
literal|"'"
argument_list|,
name|expected
argument_list|,
name|results
operator|.
name|get
argument_list|(
name|region
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|org
operator|.
name|junit
operator|.
name|Rule
specifier|public
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|ResourceCheckerJUnitRule
name|cu
init|=
operator|new
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|ResourceCheckerJUnitRule
argument_list|()
decl_stmt|;
block|}
end_class

end_unit

