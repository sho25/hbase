begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Copyright The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one or more  * contributor license agreements. See the NOTICE file distributed with this  * work for additional information regarding copyright ownership. The ASF  * licenses this file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  * http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the  * License for the specific language governing permissions and limitations  * under the License.  */
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
name|master
operator|.
name|snapshot
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
name|ArrayList
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Collections
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
name|concurrent
operator|.
name|atomic
operator|.
name|AtomicBoolean
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
name|CategoryBasedTimeout
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
name|HBaseConfiguration
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
name|Server
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
name|ServerName
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
name|RegionInfo
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
name|RegionInfoBuilder
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
name|master
operator|.
name|MasterServices
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
name|master
operator|.
name|ServerManager
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
name|master
operator|.
name|assignment
operator|.
name|AssignProcedure
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
name|master
operator|.
name|assignment
operator|.
name|AssignmentManager
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
name|master
operator|.
name|assignment
operator|.
name|RegionStates
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
name|master
operator|.
name|procedure
operator|.
name|MasterProcedureEnv
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
name|master
operator|.
name|procedure
operator|.
name|RSProcedureDispatcher
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
name|ProcedureSuspendedException
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
name|RegionServerTests
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
name|junit
operator|.
name|rules
operator|.
name|TestName
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
name|TestRule
import|;
end_import

begin_import
import|import
name|org
operator|.
name|mockito
operator|.
name|Mockito
import|;
end_import

begin_import
import|import static
name|junit
operator|.
name|framework
operator|.
name|TestCase
operator|.
name|assertFalse
import|;
end_import

begin_import
import|import static
name|junit
operator|.
name|framework
operator|.
name|TestCase
operator|.
name|assertTrue
import|;
end_import

begin_class
annotation|@
name|Category
argument_list|(
block|{
name|RegionServerTests
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
name|TestAssignProcedure
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
name|TestAssignProcedure
operator|.
name|class
argument_list|)
decl_stmt|;
annotation|@
name|Rule
specifier|public
name|TestName
name|name
init|=
operator|new
name|TestName
argument_list|()
decl_stmt|;
annotation|@
name|Rule
specifier|public
specifier|final
name|TestRule
name|timeout
init|=
name|CategoryBasedTimeout
operator|.
name|builder
argument_list|()
operator|.
name|withTimeout
argument_list|(
name|this
operator|.
name|getClass
argument_list|()
argument_list|)
operator|.
name|withLookingForStuckThread
argument_list|(
literal|true
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
comment|/**    * An override that opens up the updateTransition method inside in AssignProcedure so can call it    * below directly in test and mess with targetServer. Used by test    * {@link #testTargetServerBeingNulledOnUs()}.    */
specifier|public
specifier|static
class|class
name|TargetServerBeingNulledOnUsAssignProcedure
extends|extends
name|AssignProcedure
block|{
specifier|public
specifier|final
name|AtomicBoolean
name|addToRemoteDispatcherWasCalled
init|=
operator|new
name|AtomicBoolean
argument_list|(
literal|false
argument_list|)
decl_stmt|;
specifier|public
specifier|final
name|AtomicBoolean
name|remoteCallFailedWasCalled
init|=
operator|new
name|AtomicBoolean
argument_list|(
literal|false
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|RegionStates
operator|.
name|RegionStateNode
name|rsn
decl_stmt|;
specifier|public
name|TargetServerBeingNulledOnUsAssignProcedure
parameter_list|(
name|RegionInfo
name|regionInfo
parameter_list|,
name|RegionStates
operator|.
name|RegionStateNode
name|rsn
parameter_list|)
block|{
name|super
argument_list|(
name|regionInfo
argument_list|)
expr_stmt|;
name|this
operator|.
name|rsn
operator|=
name|rsn
expr_stmt|;
block|}
comment|/**      * Override so can change access from protected to public.      */
annotation|@
name|Override
specifier|public
name|boolean
name|updateTransition
parameter_list|(
name|MasterProcedureEnv
name|env
parameter_list|,
name|RegionStates
operator|.
name|RegionStateNode
name|regionNode
parameter_list|)
throws|throws
name|IOException
throws|,
name|ProcedureSuspendedException
block|{
return|return
name|super
operator|.
name|updateTransition
argument_list|(
name|env
argument_list|,
name|regionNode
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|protected
name|boolean
name|addToRemoteDispatcher
parameter_list|(
name|MasterProcedureEnv
name|env
parameter_list|,
name|ServerName
name|targetServer
parameter_list|)
block|{
comment|// So, mock the ServerCrashProcedure nulling out the targetServer AFTER updateTransition
comment|// has been called and BEFORE updateTransition gets to here.
comment|// We used to throw a NullPointerException. Now we just say the assign failed so it will
comment|// be rescheduled.
name|boolean
name|b
init|=
name|super
operator|.
name|addToRemoteDispatcher
argument_list|(
name|env
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|assertFalse
argument_list|(
name|b
argument_list|)
expr_stmt|;
comment|// Assert we were actually called.
name|this
operator|.
name|addToRemoteDispatcherWasCalled
operator|.
name|set
argument_list|(
literal|true
argument_list|)
expr_stmt|;
return|return
name|b
return|;
block|}
annotation|@
name|Override
specifier|public
name|RegionStates
operator|.
name|RegionStateNode
name|getRegionState
parameter_list|(
name|MasterProcedureEnv
name|env
parameter_list|)
block|{
comment|// Do this so we don't have to mock a bunch of stuff.
return|return
name|this
operator|.
name|rsn
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|remoteCallFailed
parameter_list|(
specifier|final
name|MasterProcedureEnv
name|env
parameter_list|,
specifier|final
name|ServerName
name|serverName
parameter_list|,
specifier|final
name|IOException
name|exception
parameter_list|)
block|{
comment|// Just skip this remoteCallFailed. Its too hard to mock. Assert it is called though.
comment|// Happens after the code we are testing has been called.
name|this
operator|.
name|remoteCallFailedWasCalled
operator|.
name|set
argument_list|(
literal|true
argument_list|)
expr_stmt|;
block|}
block|}
empty_stmt|;
comment|/**    * Test that we deal with ServerCrashProcedure zero'ing out the targetServer in the    * RegionStateNode in the midst of our doing an assign. The trickery is done above in    * TargetServerBeingNulledOnUsAssignProcedure. We skip a bunch of logic to get at the guts    * where the problem happens (We also skip-out the failure handling because it'd take a bunch    * of mocking to get it to run). Fix is inside in RemoteProcedureDispatch#addOperationToNode.    * It now notices empty targetServer and just returns false so we fall into failure processing    * and we'll reassign elsewhere instead of NPE'ing. The fake of ServerCrashProcedure nulling out    * the targetServer happens inside in updateTransition just after it was called but before it    * gets to the near the end when addToRemoteDispatcher is called. See the    * TargetServerBeingNulledOnUsAssignProcedure class above. See HBASE-19218.    * Before fix, this test would fail w/ a NullPointerException.    */
annotation|@
name|Test
specifier|public
name|void
name|testTargetServerBeingNulledOnUs
parameter_list|()
throws|throws
name|ProcedureSuspendedException
throws|,
name|IOException
block|{
name|TableName
name|tn
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
name|this
operator|.
name|name
operator|.
name|getMethodName
argument_list|()
argument_list|)
decl_stmt|;
name|RegionInfo
name|ri
init|=
name|RegionInfoBuilder
operator|.
name|newBuilder
argument_list|(
name|tn
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
comment|// Create an RSN with location/target server. Will be cleared above in addToRemoteDispatcher to
comment|// simulate issue in HBASE-19218
name|RegionStates
operator|.
name|RegionStateNode
name|rsn
init|=
operator|new
name|RegionStates
operator|.
name|RegionStateNode
argument_list|(
name|ri
argument_list|)
decl_stmt|;
name|rsn
operator|.
name|setRegionLocation
argument_list|(
name|ServerName
operator|.
name|valueOf
argument_list|(
literal|"server.example.org"
argument_list|,
literal|0
argument_list|,
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|MasterProcedureEnv
name|env
init|=
name|Mockito
operator|.
name|mock
argument_list|(
name|MasterProcedureEnv
operator|.
name|class
argument_list|)
decl_stmt|;
name|AssignmentManager
name|am
init|=
name|Mockito
operator|.
name|mock
argument_list|(
name|AssignmentManager
operator|.
name|class
argument_list|)
decl_stmt|;
name|ServerManager
name|sm
init|=
name|Mockito
operator|.
name|mock
argument_list|(
name|ServerManager
operator|.
name|class
argument_list|)
decl_stmt|;
name|Mockito
operator|.
name|when
argument_list|(
name|sm
operator|.
name|isServerOnline
argument_list|(
name|Mockito
operator|.
name|any
argument_list|()
argument_list|)
argument_list|)
operator|.
name|thenReturn
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|MasterServices
name|ms
init|=
name|Mockito
operator|.
name|mock
argument_list|(
name|MasterServices
operator|.
name|class
argument_list|)
decl_stmt|;
name|Mockito
operator|.
name|when
argument_list|(
name|ms
operator|.
name|getServerManager
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|sm
argument_list|)
expr_stmt|;
name|Configuration
name|configuration
init|=
name|HBaseConfiguration
operator|.
name|create
argument_list|()
decl_stmt|;
name|Mockito
operator|.
name|when
argument_list|(
name|ms
operator|.
name|getConfiguration
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|configuration
argument_list|)
expr_stmt|;
name|Mockito
operator|.
name|when
argument_list|(
name|env
operator|.
name|getAssignmentManager
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|am
argument_list|)
expr_stmt|;
name|Mockito
operator|.
name|when
argument_list|(
name|env
operator|.
name|getMasterServices
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|ms
argument_list|)
expr_stmt|;
name|RSProcedureDispatcher
name|rsd
init|=
operator|new
name|RSProcedureDispatcher
argument_list|(
name|ms
argument_list|)
decl_stmt|;
name|Mockito
operator|.
name|when
argument_list|(
name|env
operator|.
name|getRemoteDispatcher
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|rsd
argument_list|)
expr_stmt|;
name|TargetServerBeingNulledOnUsAssignProcedure
name|assignProcedure
init|=
operator|new
name|TargetServerBeingNulledOnUsAssignProcedure
argument_list|(
name|ri
argument_list|,
name|rsn
argument_list|)
decl_stmt|;
name|assignProcedure
operator|.
name|updateTransition
argument_list|(
name|env
argument_list|,
name|rsn
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|assignProcedure
operator|.
name|remoteCallFailedWasCalled
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|assignProcedure
operator|.
name|addToRemoteDispatcherWasCalled
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testSimpleComparator
parameter_list|()
block|{
name|List
argument_list|<
name|AssignProcedure
argument_list|>
name|procedures
init|=
operator|new
name|ArrayList
argument_list|<
name|AssignProcedure
argument_list|>
argument_list|()
decl_stmt|;
name|RegionInfo
name|user1
init|=
name|RegionInfoBuilder
operator|.
name|newBuilder
argument_list|(
name|TableName
operator|.
name|valueOf
argument_list|(
literal|"user_space1"
argument_list|)
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|procedures
operator|.
name|add
argument_list|(
operator|new
name|AssignProcedure
argument_list|(
name|user1
argument_list|)
argument_list|)
expr_stmt|;
name|RegionInfo
name|user2
init|=
name|RegionInfoBuilder
operator|.
name|newBuilder
argument_list|(
name|TableName
operator|.
name|valueOf
argument_list|(
literal|"user_space2"
argument_list|)
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|procedures
operator|.
name|add
argument_list|(
operator|new
name|AssignProcedure
argument_list|(
name|RegionInfoBuilder
operator|.
name|FIRST_META_REGIONINFO
argument_list|)
argument_list|)
expr_stmt|;
name|procedures
operator|.
name|add
argument_list|(
operator|new
name|AssignProcedure
argument_list|(
name|user2
argument_list|)
argument_list|)
expr_stmt|;
name|RegionInfo
name|system
init|=
name|RegionInfoBuilder
operator|.
name|newBuilder
argument_list|(
name|TableName
operator|.
name|NAMESPACE_TABLE_NAME
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|procedures
operator|.
name|add
argument_list|(
operator|new
name|AssignProcedure
argument_list|(
name|system
argument_list|)
argument_list|)
expr_stmt|;
name|procedures
operator|.
name|sort
argument_list|(
name|AssignProcedure
operator|.
name|COMPARATOR
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|procedures
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|isMeta
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|procedures
operator|.
name|get
argument_list|(
literal|1
argument_list|)
operator|.
name|getRegionInfo
argument_list|()
operator|.
name|getTable
argument_list|()
operator|.
name|equals
argument_list|(
name|TableName
operator|.
name|NAMESPACE_TABLE_NAME
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testComparatorWithMetas
parameter_list|()
block|{
name|List
argument_list|<
name|AssignProcedure
argument_list|>
name|procedures
init|=
operator|new
name|ArrayList
argument_list|<
name|AssignProcedure
argument_list|>
argument_list|()
decl_stmt|;
name|RegionInfo
name|user3
init|=
name|RegionInfoBuilder
operator|.
name|newBuilder
argument_list|(
name|TableName
operator|.
name|valueOf
argument_list|(
literal|"user3"
argument_list|)
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|procedures
operator|.
name|add
argument_list|(
operator|new
name|AssignProcedure
argument_list|(
name|user3
argument_list|)
argument_list|)
expr_stmt|;
name|RegionInfo
name|system
init|=
name|RegionInfoBuilder
operator|.
name|newBuilder
argument_list|(
name|TableName
operator|.
name|NAMESPACE_TABLE_NAME
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|procedures
operator|.
name|add
argument_list|(
operator|new
name|AssignProcedure
argument_list|(
name|system
argument_list|)
argument_list|)
expr_stmt|;
name|RegionInfo
name|user1
init|=
name|RegionInfoBuilder
operator|.
name|newBuilder
argument_list|(
name|TableName
operator|.
name|valueOf
argument_list|(
literal|"user_space1"
argument_list|)
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|RegionInfo
name|user2
init|=
name|RegionInfoBuilder
operator|.
name|newBuilder
argument_list|(
name|TableName
operator|.
name|valueOf
argument_list|(
literal|"user_space2"
argument_list|)
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|procedures
operator|.
name|add
argument_list|(
operator|new
name|AssignProcedure
argument_list|(
name|user1
argument_list|)
argument_list|)
expr_stmt|;
name|RegionInfo
name|meta2
init|=
name|RegionInfoBuilder
operator|.
name|newBuilder
argument_list|(
name|TableName
operator|.
name|META_TABLE_NAME
argument_list|)
operator|.
name|setStartKey
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"002"
argument_list|)
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|procedures
operator|.
name|add
argument_list|(
operator|new
name|AssignProcedure
argument_list|(
name|meta2
argument_list|)
argument_list|)
expr_stmt|;
name|procedures
operator|.
name|add
argument_list|(
operator|new
name|AssignProcedure
argument_list|(
name|user2
argument_list|)
argument_list|)
expr_stmt|;
name|RegionInfo
name|meta1
init|=
name|RegionInfoBuilder
operator|.
name|newBuilder
argument_list|(
name|TableName
operator|.
name|META_TABLE_NAME
argument_list|)
operator|.
name|setStartKey
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"001"
argument_list|)
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|procedures
operator|.
name|add
argument_list|(
operator|new
name|AssignProcedure
argument_list|(
name|meta1
argument_list|)
argument_list|)
expr_stmt|;
name|procedures
operator|.
name|add
argument_list|(
operator|new
name|AssignProcedure
argument_list|(
name|RegionInfoBuilder
operator|.
name|FIRST_META_REGIONINFO
argument_list|)
argument_list|)
expr_stmt|;
name|RegionInfo
name|meta0
init|=
name|RegionInfoBuilder
operator|.
name|newBuilder
argument_list|(
name|TableName
operator|.
name|META_TABLE_NAME
argument_list|)
operator|.
name|setStartKey
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"000"
argument_list|)
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|procedures
operator|.
name|add
argument_list|(
operator|new
name|AssignProcedure
argument_list|(
name|meta0
argument_list|)
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
literal|10
condition|;
name|i
operator|++
control|)
block|{
name|Collections
operator|.
name|shuffle
argument_list|(
name|procedures
argument_list|)
expr_stmt|;
name|procedures
operator|.
name|sort
argument_list|(
name|AssignProcedure
operator|.
name|COMPARATOR
argument_list|)
expr_stmt|;
try|try
block|{
name|assertTrue
argument_list|(
name|procedures
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getRegionInfo
argument_list|()
operator|.
name|equals
argument_list|(
name|RegionInfoBuilder
operator|.
name|FIRST_META_REGIONINFO
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|procedures
operator|.
name|get
argument_list|(
literal|1
argument_list|)
operator|.
name|getRegionInfo
argument_list|()
operator|.
name|equals
argument_list|(
name|meta0
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|procedures
operator|.
name|get
argument_list|(
literal|2
argument_list|)
operator|.
name|getRegionInfo
argument_list|()
operator|.
name|equals
argument_list|(
name|meta1
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|procedures
operator|.
name|get
argument_list|(
literal|3
argument_list|)
operator|.
name|getRegionInfo
argument_list|()
operator|.
name|equals
argument_list|(
name|meta2
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|procedures
operator|.
name|get
argument_list|(
literal|4
argument_list|)
operator|.
name|getRegionInfo
argument_list|()
operator|.
name|getTable
argument_list|()
operator|.
name|equals
argument_list|(
name|TableName
operator|.
name|NAMESPACE_TABLE_NAME
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|procedures
operator|.
name|get
argument_list|(
literal|5
argument_list|)
operator|.
name|getRegionInfo
argument_list|()
operator|.
name|equals
argument_list|(
name|user1
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|procedures
operator|.
name|get
argument_list|(
literal|6
argument_list|)
operator|.
name|getRegionInfo
argument_list|()
operator|.
name|equals
argument_list|(
name|user2
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|procedures
operator|.
name|get
argument_list|(
literal|7
argument_list|)
operator|.
name|getRegionInfo
argument_list|()
operator|.
name|equals
argument_list|(
name|user3
argument_list|)
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Throwable
name|t
parameter_list|)
block|{
for|for
control|(
name|AssignProcedure
name|proc
range|:
name|procedures
control|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
name|proc
argument_list|)
expr_stmt|;
block|}
throw|throw
name|t
throw|;
block|}
block|}
block|}
block|}
end_class

end_unit

