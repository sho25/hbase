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
name|procedure2
operator|.
name|store
operator|.
name|region
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
name|net
operator|.
name|InetAddress
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|HashSet
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Optional
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Set
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
name|HBaseClassTestRule
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
name|ipc
operator|.
name|RpcCall
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
name|RpcCallback
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
name|RpcServer
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
name|procedure2
operator|.
name|Procedure
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
name|procedure2
operator|.
name|ProcedureTestingUtility
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
name|procedure2
operator|.
name|ProcedureTestingUtility
operator|.
name|LoadCounter
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
name|testclassification
operator|.
name|MasterTests
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
name|ClassRule
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
name|slf4j
operator|.
name|Logger
import|;
end_import

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|LoggerFactory
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hbase
operator|.
name|thirdparty
operator|.
name|com
operator|.
name|google
operator|.
name|protobuf
operator|.
name|BlockingService
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hbase
operator|.
name|thirdparty
operator|.
name|com
operator|.
name|google
operator|.
name|protobuf
operator|.
name|Descriptors
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hbase
operator|.
name|thirdparty
operator|.
name|com
operator|.
name|google
operator|.
name|protobuf
operator|.
name|Message
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
name|protobuf
operator|.
name|generated
operator|.
name|HBaseProtos
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
name|protobuf
operator|.
name|generated
operator|.
name|RPCProtos
import|;
end_import

begin_class
annotation|@
name|Category
argument_list|(
block|{
name|MasterTests
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
name|TestRegionProcedureStore
extends|extends
name|RegionProcedureStoreTestBase
block|{
annotation|@
name|ClassRule
specifier|public
specifier|static
specifier|final
name|HBaseClassTestRule
name|CLASS_RULE
init|=
name|HBaseClassTestRule
operator|.
name|forClass
argument_list|(
name|TestRegionProcedureStore
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|TestRegionProcedureStore
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
name|void
name|verifyProcIdsOnRestart
parameter_list|(
specifier|final
name|Set
argument_list|<
name|Long
argument_list|>
name|procIds
parameter_list|)
throws|throws
name|Exception
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"expected: "
operator|+
name|procIds
argument_list|)
expr_stmt|;
name|LoadCounter
name|loader
init|=
operator|new
name|LoadCounter
argument_list|()
decl_stmt|;
name|ProcedureTestingUtility
operator|.
name|storeRestart
argument_list|(
name|store
argument_list|,
literal|true
argument_list|,
name|loader
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|procIds
operator|.
name|size
argument_list|()
argument_list|,
name|loader
operator|.
name|getLoadedCount
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|loader
operator|.
name|getCorruptedCount
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testLoad
parameter_list|()
throws|throws
name|Exception
block|{
name|Set
argument_list|<
name|Long
argument_list|>
name|procIds
init|=
operator|new
name|HashSet
argument_list|<>
argument_list|()
decl_stmt|;
comment|// Insert something in the log
name|RegionProcedureStoreTestProcedure
name|proc1
init|=
operator|new
name|RegionProcedureStoreTestProcedure
argument_list|()
decl_stmt|;
name|procIds
operator|.
name|add
argument_list|(
name|proc1
operator|.
name|getProcId
argument_list|()
argument_list|)
expr_stmt|;
name|store
operator|.
name|insert
argument_list|(
name|proc1
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|RegionProcedureStoreTestProcedure
name|proc2
init|=
operator|new
name|RegionProcedureStoreTestProcedure
argument_list|()
decl_stmt|;
name|RegionProcedureStoreTestProcedure
name|proc3
init|=
operator|new
name|RegionProcedureStoreTestProcedure
argument_list|()
decl_stmt|;
name|proc3
operator|.
name|setParent
argument_list|(
name|proc2
argument_list|)
expr_stmt|;
name|RegionProcedureStoreTestProcedure
name|proc4
init|=
operator|new
name|RegionProcedureStoreTestProcedure
argument_list|()
decl_stmt|;
name|proc4
operator|.
name|setParent
argument_list|(
name|proc2
argument_list|)
expr_stmt|;
name|procIds
operator|.
name|add
argument_list|(
name|proc2
operator|.
name|getProcId
argument_list|()
argument_list|)
expr_stmt|;
name|procIds
operator|.
name|add
argument_list|(
name|proc3
operator|.
name|getProcId
argument_list|()
argument_list|)
expr_stmt|;
name|procIds
operator|.
name|add
argument_list|(
name|proc4
operator|.
name|getProcId
argument_list|()
argument_list|)
expr_stmt|;
name|store
operator|.
name|insert
argument_list|(
name|proc2
argument_list|,
operator|new
name|Procedure
index|[]
block|{
name|proc3
block|,
name|proc4
block|}
argument_list|)
expr_stmt|;
comment|// Verify that everything is there
name|verifyProcIdsOnRestart
argument_list|(
name|procIds
argument_list|)
expr_stmt|;
comment|// Update and delete something
name|proc1
operator|.
name|finish
argument_list|()
expr_stmt|;
name|store
operator|.
name|update
argument_list|(
name|proc1
argument_list|)
expr_stmt|;
name|proc4
operator|.
name|finish
argument_list|()
expr_stmt|;
name|store
operator|.
name|update
argument_list|(
name|proc4
argument_list|)
expr_stmt|;
name|store
operator|.
name|delete
argument_list|(
name|proc4
operator|.
name|getProcId
argument_list|()
argument_list|)
expr_stmt|;
name|procIds
operator|.
name|remove
argument_list|(
name|proc4
operator|.
name|getProcId
argument_list|()
argument_list|)
expr_stmt|;
comment|// Verify that everything is there
name|verifyProcIdsOnRestart
argument_list|(
name|procIds
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testCleanup
parameter_list|()
throws|throws
name|Exception
block|{
name|RegionProcedureStoreTestProcedure
name|proc1
init|=
operator|new
name|RegionProcedureStoreTestProcedure
argument_list|()
decl_stmt|;
name|store
operator|.
name|insert
argument_list|(
name|proc1
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|RegionProcedureStoreTestProcedure
name|proc2
init|=
operator|new
name|RegionProcedureStoreTestProcedure
argument_list|()
decl_stmt|;
name|store
operator|.
name|insert
argument_list|(
name|proc2
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|RegionProcedureStoreTestProcedure
name|proc3
init|=
operator|new
name|RegionProcedureStoreTestProcedure
argument_list|()
decl_stmt|;
name|store
operator|.
name|insert
argument_list|(
name|proc3
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|LoadCounter
name|loader
init|=
operator|new
name|LoadCounter
argument_list|()
decl_stmt|;
name|store
operator|.
name|load
argument_list|(
name|loader
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|proc3
operator|.
name|getProcId
argument_list|()
argument_list|,
name|loader
operator|.
name|getMaxProcId
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|3
argument_list|,
name|loader
operator|.
name|getRunnableCount
argument_list|()
argument_list|)
expr_stmt|;
name|store
operator|.
name|delete
argument_list|(
name|proc3
operator|.
name|getProcId
argument_list|()
argument_list|)
expr_stmt|;
name|store
operator|.
name|delete
argument_list|(
name|proc2
operator|.
name|getProcId
argument_list|()
argument_list|)
expr_stmt|;
name|loader
operator|=
operator|new
name|LoadCounter
argument_list|()
expr_stmt|;
name|store
operator|.
name|load
argument_list|(
name|loader
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|proc3
operator|.
name|getProcId
argument_list|()
argument_list|,
name|loader
operator|.
name|getMaxProcId
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|loader
operator|.
name|getRunnableCount
argument_list|()
argument_list|)
expr_stmt|;
comment|// the row should still be there
name|assertTrue
argument_list|(
name|store
operator|.
name|region
operator|.
name|get
argument_list|(
operator|new
name|Get
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|proc3
operator|.
name|getProcId
argument_list|()
argument_list|)
argument_list|)
operator|.
name|setCheckExistenceOnly
argument_list|(
literal|true
argument_list|)
argument_list|)
operator|.
name|getExists
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|store
operator|.
name|region
operator|.
name|get
argument_list|(
operator|new
name|Get
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|proc2
operator|.
name|getProcId
argument_list|()
argument_list|)
argument_list|)
operator|.
name|setCheckExistenceOnly
argument_list|(
literal|true
argument_list|)
argument_list|)
operator|.
name|getExists
argument_list|()
argument_list|)
expr_stmt|;
comment|// proc2 will be deleted after cleanup, but proc3 should still be there as it holds the max proc
comment|// id
name|store
operator|.
name|cleanup
argument_list|()
expr_stmt|;
name|assertTrue
argument_list|(
name|store
operator|.
name|region
operator|.
name|get
argument_list|(
operator|new
name|Get
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|proc3
operator|.
name|getProcId
argument_list|()
argument_list|)
argument_list|)
operator|.
name|setCheckExistenceOnly
argument_list|(
literal|true
argument_list|)
argument_list|)
operator|.
name|getExists
argument_list|()
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|store
operator|.
name|region
operator|.
name|get
argument_list|(
operator|new
name|Get
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|proc2
operator|.
name|getProcId
argument_list|()
argument_list|)
argument_list|)
operator|.
name|setCheckExistenceOnly
argument_list|(
literal|true
argument_list|)
argument_list|)
operator|.
name|getExists
argument_list|()
argument_list|)
expr_stmt|;
name|RegionProcedureStoreTestProcedure
name|proc4
init|=
operator|new
name|RegionProcedureStoreTestProcedure
argument_list|()
decl_stmt|;
name|store
operator|.
name|insert
argument_list|(
name|proc4
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|store
operator|.
name|cleanup
argument_list|()
expr_stmt|;
comment|// proc3 should also be deleted as now proc4 holds the max proc id
name|assertFalse
argument_list|(
name|store
operator|.
name|region
operator|.
name|get
argument_list|(
operator|new
name|Get
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|proc3
operator|.
name|getProcId
argument_list|()
argument_list|)
argument_list|)
operator|.
name|setCheckExistenceOnly
argument_list|(
literal|true
argument_list|)
argument_list|)
operator|.
name|getExists
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|/**    * Test for HBASE-23895    */
annotation|@
name|Test
specifier|public
name|void
name|testInsertWithRpcCall
parameter_list|()
throws|throws
name|Exception
block|{
name|RpcServer
operator|.
name|setCurrentCall
argument_list|(
name|newRpcCallWithDeadline
argument_list|()
argument_list|)
expr_stmt|;
name|RegionProcedureStoreTestProcedure
name|proc1
init|=
operator|new
name|RegionProcedureStoreTestProcedure
argument_list|()
decl_stmt|;
name|store
operator|.
name|insert
argument_list|(
name|proc1
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|RpcServer
operator|.
name|setCurrentCall
argument_list|(
literal|null
argument_list|)
expr_stmt|;
block|}
specifier|private
name|RpcCall
name|newRpcCallWithDeadline
parameter_list|()
block|{
return|return
operator|new
name|RpcCall
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|long
name|getDeadline
parameter_list|()
block|{
return|return
name|System
operator|.
name|currentTimeMillis
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|BlockingService
name|getService
parameter_list|()
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
name|Descriptors
operator|.
name|MethodDescriptor
name|getMethod
parameter_list|()
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
name|Message
name|getParam
parameter_list|()
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
name|CellScanner
name|getCellScanner
parameter_list|()
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|getReceiveTime
parameter_list|()
block|{
return|return
literal|0
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|getStartTime
parameter_list|()
block|{
return|return
literal|0
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|setStartTime
parameter_list|(
name|long
name|startTime
parameter_list|)
block|{        }
annotation|@
name|Override
specifier|public
name|int
name|getTimeout
parameter_list|()
block|{
return|return
literal|0
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|getPriority
parameter_list|()
block|{
return|return
literal|0
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|getSize
parameter_list|()
block|{
return|return
literal|0
return|;
block|}
annotation|@
name|Override
specifier|public
name|RPCProtos
operator|.
name|RequestHeader
name|getHeader
parameter_list|()
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|getRemotePort
parameter_list|()
block|{
return|return
literal|0
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|setResponse
parameter_list|(
name|Message
name|param
parameter_list|,
name|CellScanner
name|cells
parameter_list|,
name|Throwable
name|errorThrowable
parameter_list|,
name|String
name|error
parameter_list|)
block|{       }
annotation|@
name|Override
specifier|public
name|void
name|sendResponseIfReady
parameter_list|()
throws|throws
name|IOException
block|{       }
annotation|@
name|Override
specifier|public
name|void
name|cleanup
parameter_list|()
block|{       }
annotation|@
name|Override
specifier|public
name|String
name|toShortString
parameter_list|()
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|disconnectSince
parameter_list|()
block|{
return|return
literal|0
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|isClientCellBlockSupported
parameter_list|()
block|{
return|return
literal|false
return|;
block|}
annotation|@
name|Override
specifier|public
name|Optional
argument_list|<
name|User
argument_list|>
name|getRequestUser
parameter_list|()
block|{
return|return
name|Optional
operator|.
name|empty
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|InetAddress
name|getRemoteAddress
parameter_list|()
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
name|HBaseProtos
operator|.
name|VersionInfo
name|getClientVersionInfo
parameter_list|()
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|setCallBack
parameter_list|(
name|RpcCallback
name|callback
parameter_list|)
block|{       }
annotation|@
name|Override
specifier|public
name|boolean
name|isRetryImmediatelySupported
parameter_list|()
block|{
return|return
literal|false
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|getResponseCellSize
parameter_list|()
block|{
return|return
literal|0
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|incrementResponseCellSize
parameter_list|(
name|long
name|cellSize
parameter_list|)
block|{       }
annotation|@
name|Override
specifier|public
name|long
name|getResponseBlockSize
parameter_list|()
block|{
return|return
literal|0
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|incrementResponseBlockSize
parameter_list|(
name|long
name|blockSize
parameter_list|)
block|{       }
annotation|@
name|Override
specifier|public
name|long
name|getResponseExceptionSize
parameter_list|()
block|{
return|return
literal|0
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|incrementResponseExceptionSize
parameter_list|(
name|long
name|exceptionSize
parameter_list|)
block|{       }
block|}
return|;
block|}
block|}
end_class

end_unit

