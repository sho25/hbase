begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Copyright 2010 The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  * http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|assertNotNull
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
name|Collection
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
name|HServerAddress
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
name|UnknownRegionException
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
name|CoprocessorEnvironment
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
name|master
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
name|HMaster
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
name|MasterCoprocessorHost
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
name|HRegionServer
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
name|Test
import|;
end_import

begin_comment
comment|/**  * Tests invocation of the {@link org.apache.hadoop.hbase.coprocessor.MasterObserver}  * interface hooks at all appropriate times during normal HMaster operations.  */
end_comment

begin_class
specifier|public
class|class
name|TestMasterObserver
block|{
specifier|public
specifier|static
class|class
name|CPMasterObserver
implements|implements
name|MasterObserver
block|{
specifier|private
name|boolean
name|preCreateTableCalled
decl_stmt|;
specifier|private
name|boolean
name|postCreateTableCalled
decl_stmt|;
specifier|private
name|boolean
name|preDeleteTableCalled
decl_stmt|;
specifier|private
name|boolean
name|postDeleteTableCalled
decl_stmt|;
specifier|private
name|boolean
name|preModifyTableCalled
decl_stmt|;
specifier|private
name|boolean
name|postModifyTableCalled
decl_stmt|;
specifier|private
name|boolean
name|preAddColumnCalled
decl_stmt|;
specifier|private
name|boolean
name|postAddColumnCalled
decl_stmt|;
specifier|private
name|boolean
name|preModifyColumnCalled
decl_stmt|;
specifier|private
name|boolean
name|postModifyColumnCalled
decl_stmt|;
specifier|private
name|boolean
name|preDeleteColumnCalled
decl_stmt|;
specifier|private
name|boolean
name|postDeleteColumnCalled
decl_stmt|;
specifier|private
name|boolean
name|preEnableTableCalled
decl_stmt|;
specifier|private
name|boolean
name|postEnableTableCalled
decl_stmt|;
specifier|private
name|boolean
name|preDisableTableCalled
decl_stmt|;
specifier|private
name|boolean
name|postDisableTableCalled
decl_stmt|;
specifier|private
name|boolean
name|preMoveCalled
decl_stmt|;
specifier|private
name|boolean
name|postMoveCalled
decl_stmt|;
specifier|private
name|boolean
name|preAssignCalled
decl_stmt|;
specifier|private
name|boolean
name|postAssignCalled
decl_stmt|;
specifier|private
name|boolean
name|preUnassignCalled
decl_stmt|;
specifier|private
name|boolean
name|postUnassignCalled
decl_stmt|;
specifier|private
name|boolean
name|preBalanceCalled
decl_stmt|;
specifier|private
name|boolean
name|postBalanceCalled
decl_stmt|;
specifier|private
name|boolean
name|preBalanceSwitchCalled
decl_stmt|;
specifier|private
name|boolean
name|postBalanceSwitchCalled
decl_stmt|;
specifier|private
name|boolean
name|preShutdownCalled
decl_stmt|;
specifier|private
name|boolean
name|preStopMasterCalled
decl_stmt|;
specifier|private
name|boolean
name|postStartMasterCalled
decl_stmt|;
specifier|private
name|boolean
name|startCalled
decl_stmt|;
specifier|private
name|boolean
name|stopCalled
decl_stmt|;
annotation|@
name|Override
specifier|public
name|void
name|preCreateTable
parameter_list|(
name|ObserverContext
argument_list|<
name|MasterCoprocessorEnvironment
argument_list|>
name|env
parameter_list|,
name|HTableDescriptor
name|desc
parameter_list|,
name|byte
index|[]
index|[]
name|splitKeys
parameter_list|)
throws|throws
name|IOException
block|{
name|preCreateTableCalled
operator|=
literal|true
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|postCreateTable
parameter_list|(
name|ObserverContext
argument_list|<
name|MasterCoprocessorEnvironment
argument_list|>
name|env
parameter_list|,
name|HRegionInfo
index|[]
name|regions
parameter_list|,
name|boolean
name|sync
parameter_list|)
throws|throws
name|IOException
block|{
name|postCreateTableCalled
operator|=
literal|true
expr_stmt|;
block|}
specifier|public
name|boolean
name|wasCreateTableCalled
parameter_list|()
block|{
return|return
name|preCreateTableCalled
operator|&&
name|postCreateTableCalled
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|preDeleteTable
parameter_list|(
name|ObserverContext
argument_list|<
name|MasterCoprocessorEnvironment
argument_list|>
name|env
parameter_list|,
name|byte
index|[]
name|tableName
parameter_list|)
throws|throws
name|IOException
block|{
name|preDeleteTableCalled
operator|=
literal|true
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|postDeleteTable
parameter_list|(
name|ObserverContext
argument_list|<
name|MasterCoprocessorEnvironment
argument_list|>
name|env
parameter_list|,
name|byte
index|[]
name|tableName
parameter_list|)
throws|throws
name|IOException
block|{
name|postDeleteTableCalled
operator|=
literal|true
expr_stmt|;
block|}
specifier|public
name|boolean
name|wasDeleteTableCalled
parameter_list|()
block|{
return|return
name|preDeleteTableCalled
operator|&&
name|postDeleteTableCalled
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|preModifyTable
parameter_list|(
name|ObserverContext
argument_list|<
name|MasterCoprocessorEnvironment
argument_list|>
name|env
parameter_list|,
name|byte
index|[]
name|tableName
parameter_list|,
name|HTableDescriptor
name|htd
parameter_list|)
throws|throws
name|IOException
block|{
name|preModifyTableCalled
operator|=
literal|true
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|postModifyTable
parameter_list|(
name|ObserverContext
argument_list|<
name|MasterCoprocessorEnvironment
argument_list|>
name|env
parameter_list|,
name|byte
index|[]
name|tableName
parameter_list|,
name|HTableDescriptor
name|htd
parameter_list|)
throws|throws
name|IOException
block|{
name|postModifyTableCalled
operator|=
literal|true
expr_stmt|;
block|}
specifier|public
name|boolean
name|wasModifyTableCalled
parameter_list|()
block|{
return|return
name|preModifyTableCalled
operator|&&
name|postModifyTableCalled
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|preAddColumn
parameter_list|(
name|ObserverContext
argument_list|<
name|MasterCoprocessorEnvironment
argument_list|>
name|env
parameter_list|,
name|byte
index|[]
name|tableName
parameter_list|,
name|HColumnDescriptor
name|column
parameter_list|)
throws|throws
name|IOException
block|{
name|preAddColumnCalled
operator|=
literal|true
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|postAddColumn
parameter_list|(
name|ObserverContext
argument_list|<
name|MasterCoprocessorEnvironment
argument_list|>
name|env
parameter_list|,
name|byte
index|[]
name|tableName
parameter_list|,
name|HColumnDescriptor
name|column
parameter_list|)
throws|throws
name|IOException
block|{
name|postAddColumnCalled
operator|=
literal|true
expr_stmt|;
block|}
specifier|public
name|boolean
name|wasAddColumnCalled
parameter_list|()
block|{
return|return
name|preAddColumnCalled
operator|&&
name|postAddColumnCalled
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|preModifyColumn
parameter_list|(
name|ObserverContext
argument_list|<
name|MasterCoprocessorEnvironment
argument_list|>
name|env
parameter_list|,
name|byte
index|[]
name|tableName
parameter_list|,
name|HColumnDescriptor
name|descriptor
parameter_list|)
throws|throws
name|IOException
block|{
name|preModifyColumnCalled
operator|=
literal|true
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|postModifyColumn
parameter_list|(
name|ObserverContext
argument_list|<
name|MasterCoprocessorEnvironment
argument_list|>
name|env
parameter_list|,
name|byte
index|[]
name|tableName
parameter_list|,
name|HColumnDescriptor
name|descriptor
parameter_list|)
throws|throws
name|IOException
block|{
name|postModifyColumnCalled
operator|=
literal|true
expr_stmt|;
block|}
specifier|public
name|boolean
name|wasModifyColumnCalled
parameter_list|()
block|{
return|return
name|preModifyColumnCalled
operator|&&
name|postModifyColumnCalled
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|preDeleteColumn
parameter_list|(
name|ObserverContext
argument_list|<
name|MasterCoprocessorEnvironment
argument_list|>
name|env
parameter_list|,
name|byte
index|[]
name|tableName
parameter_list|,
name|byte
index|[]
name|c
parameter_list|)
throws|throws
name|IOException
block|{
name|preDeleteColumnCalled
operator|=
literal|true
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|postDeleteColumn
parameter_list|(
name|ObserverContext
argument_list|<
name|MasterCoprocessorEnvironment
argument_list|>
name|env
parameter_list|,
name|byte
index|[]
name|tableName
parameter_list|,
name|byte
index|[]
name|c
parameter_list|)
throws|throws
name|IOException
block|{
name|postDeleteColumnCalled
operator|=
literal|true
expr_stmt|;
block|}
specifier|public
name|boolean
name|wasDeleteColumnCalled
parameter_list|()
block|{
return|return
name|preDeleteColumnCalled
operator|&&
name|postDeleteColumnCalled
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|preEnableTable
parameter_list|(
name|ObserverContext
argument_list|<
name|MasterCoprocessorEnvironment
argument_list|>
name|env
parameter_list|,
name|byte
index|[]
name|tableName
parameter_list|)
throws|throws
name|IOException
block|{
name|preEnableTableCalled
operator|=
literal|true
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|postEnableTable
parameter_list|(
name|ObserverContext
argument_list|<
name|MasterCoprocessorEnvironment
argument_list|>
name|env
parameter_list|,
name|byte
index|[]
name|tableName
parameter_list|)
throws|throws
name|IOException
block|{
name|postEnableTableCalled
operator|=
literal|true
expr_stmt|;
block|}
specifier|public
name|boolean
name|wasEnableTableCalled
parameter_list|()
block|{
return|return
name|preEnableTableCalled
operator|&&
name|postEnableTableCalled
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|preDisableTable
parameter_list|(
name|ObserverContext
argument_list|<
name|MasterCoprocessorEnvironment
argument_list|>
name|env
parameter_list|,
name|byte
index|[]
name|tableName
parameter_list|)
throws|throws
name|IOException
block|{
name|preDisableTableCalled
operator|=
literal|true
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|postDisableTable
parameter_list|(
name|ObserverContext
argument_list|<
name|MasterCoprocessorEnvironment
argument_list|>
name|env
parameter_list|,
name|byte
index|[]
name|tableName
parameter_list|)
throws|throws
name|IOException
block|{
name|postDisableTableCalled
operator|=
literal|true
expr_stmt|;
block|}
specifier|public
name|boolean
name|wasDisableTableCalled
parameter_list|()
block|{
return|return
name|preDisableTableCalled
operator|&&
name|postDisableTableCalled
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|preMove
parameter_list|(
name|ObserverContext
argument_list|<
name|MasterCoprocessorEnvironment
argument_list|>
name|env
parameter_list|,
name|HRegionInfo
name|region
parameter_list|,
name|ServerName
name|srcServer
parameter_list|,
name|ServerName
name|destServer
parameter_list|)
throws|throws
name|UnknownRegionException
block|{
name|preMoveCalled
operator|=
literal|true
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|postMove
parameter_list|(
name|ObserverContext
argument_list|<
name|MasterCoprocessorEnvironment
argument_list|>
name|env
parameter_list|,
name|HRegionInfo
name|region
parameter_list|,
name|ServerName
name|srcServer
parameter_list|,
name|ServerName
name|destServer
parameter_list|)
throws|throws
name|UnknownRegionException
block|{
name|postMoveCalled
operator|=
literal|true
expr_stmt|;
block|}
specifier|public
name|boolean
name|wasMoveCalled
parameter_list|()
block|{
return|return
name|preMoveCalled
operator|&&
name|postMoveCalled
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|preAssign
parameter_list|(
name|ObserverContext
argument_list|<
name|MasterCoprocessorEnvironment
argument_list|>
name|env
parameter_list|,
specifier|final
name|byte
index|[]
name|regionName
parameter_list|,
specifier|final
name|boolean
name|force
parameter_list|)
throws|throws
name|IOException
block|{
name|preAssignCalled
operator|=
literal|true
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|postAssign
parameter_list|(
name|ObserverContext
argument_list|<
name|MasterCoprocessorEnvironment
argument_list|>
name|env
parameter_list|,
specifier|final
name|HRegionInfo
name|regionInfo
parameter_list|)
throws|throws
name|IOException
block|{
name|postAssignCalled
operator|=
literal|true
expr_stmt|;
block|}
specifier|public
name|boolean
name|wasAssignCalled
parameter_list|()
block|{
return|return
name|preAssignCalled
operator|&&
name|postAssignCalled
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|preUnassign
parameter_list|(
name|ObserverContext
argument_list|<
name|MasterCoprocessorEnvironment
argument_list|>
name|env
parameter_list|,
specifier|final
name|byte
index|[]
name|regionName
parameter_list|,
specifier|final
name|boolean
name|force
parameter_list|)
throws|throws
name|IOException
block|{
name|preUnassignCalled
operator|=
literal|true
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|postUnassign
parameter_list|(
name|ObserverContext
argument_list|<
name|MasterCoprocessorEnvironment
argument_list|>
name|env
parameter_list|,
specifier|final
name|HRegionInfo
name|regionInfo
parameter_list|,
specifier|final
name|boolean
name|force
parameter_list|)
throws|throws
name|IOException
block|{
name|postUnassignCalled
operator|=
literal|true
expr_stmt|;
block|}
specifier|public
name|boolean
name|wasUnassignCalled
parameter_list|()
block|{
return|return
name|preUnassignCalled
operator|&&
name|postUnassignCalled
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|preBalance
parameter_list|(
name|ObserverContext
argument_list|<
name|MasterCoprocessorEnvironment
argument_list|>
name|env
parameter_list|)
throws|throws
name|IOException
block|{
name|preBalanceCalled
operator|=
literal|true
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|postBalance
parameter_list|(
name|ObserverContext
argument_list|<
name|MasterCoprocessorEnvironment
argument_list|>
name|env
parameter_list|)
throws|throws
name|IOException
block|{
name|postBalanceCalled
operator|=
literal|true
expr_stmt|;
block|}
specifier|public
name|boolean
name|wasBalanceCalled
parameter_list|()
block|{
return|return
name|preBalanceCalled
operator|&&
name|postBalanceCalled
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|preBalanceSwitch
parameter_list|(
name|ObserverContext
argument_list|<
name|MasterCoprocessorEnvironment
argument_list|>
name|env
parameter_list|,
name|boolean
name|b
parameter_list|)
throws|throws
name|IOException
block|{
name|preBalanceSwitchCalled
operator|=
literal|true
expr_stmt|;
return|return
name|b
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|postBalanceSwitch
parameter_list|(
name|ObserverContext
argument_list|<
name|MasterCoprocessorEnvironment
argument_list|>
name|env
parameter_list|,
name|boolean
name|oldValue
parameter_list|,
name|boolean
name|newValue
parameter_list|)
throws|throws
name|IOException
block|{
name|postBalanceSwitchCalled
operator|=
literal|true
expr_stmt|;
block|}
specifier|public
name|boolean
name|wasBalanceSwitchCalled
parameter_list|()
block|{
return|return
name|preBalanceSwitchCalled
operator|&&
name|postBalanceSwitchCalled
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|preShutdown
parameter_list|(
name|ObserverContext
argument_list|<
name|MasterCoprocessorEnvironment
argument_list|>
name|env
parameter_list|)
throws|throws
name|IOException
block|{
name|preShutdownCalled
operator|=
literal|true
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|preStopMaster
parameter_list|(
name|ObserverContext
argument_list|<
name|MasterCoprocessorEnvironment
argument_list|>
name|env
parameter_list|)
throws|throws
name|IOException
block|{
name|preStopMasterCalled
operator|=
literal|true
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|postStartMaster
parameter_list|(
name|ObserverContext
argument_list|<
name|MasterCoprocessorEnvironment
argument_list|>
name|ctx
parameter_list|)
throws|throws
name|IOException
block|{
name|postStartMasterCalled
operator|=
literal|true
expr_stmt|;
block|}
specifier|public
name|boolean
name|wasStartMasterCalled
parameter_list|()
block|{
return|return
name|postStartMasterCalled
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
block|{
name|startCalled
operator|=
literal|true
expr_stmt|;
block|}
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
block|{
name|stopCalled
operator|=
literal|true
expr_stmt|;
block|}
specifier|public
name|boolean
name|wasStarted
parameter_list|()
block|{
return|return
name|startCalled
return|;
block|}
specifier|public
name|boolean
name|wasStopped
parameter_list|()
block|{
return|return
name|stopCalled
return|;
block|}
block|}
specifier|private
specifier|static
name|HBaseTestingUtility
name|UTIL
init|=
operator|new
name|HBaseTestingUtility
argument_list|()
decl_stmt|;
specifier|private
specifier|static
name|byte
index|[]
name|TEST_TABLE
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"observed_table"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
name|byte
index|[]
name|TEST_FAMILY
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"fam1"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
name|byte
index|[]
name|TEST_FAMILY2
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"fam2"
argument_list|)
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
name|Configuration
name|conf
init|=
name|UTIL
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
name|MASTER_COPROCESSOR_CONF_KEY
argument_list|,
name|CPMasterObserver
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|UTIL
operator|.
name|startMiniCluster
argument_list|(
literal|2
argument_list|)
expr_stmt|;
block|}
annotation|@
name|AfterClass
specifier|public
specifier|static
name|void
name|teardownAfterClass
parameter_list|()
throws|throws
name|Exception
block|{
name|UTIL
operator|.
name|shutdownMiniCluster
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testStarted
parameter_list|()
throws|throws
name|Exception
block|{
name|MiniHBaseCluster
name|cluster
init|=
name|UTIL
operator|.
name|getHBaseCluster
argument_list|()
decl_stmt|;
name|HMaster
name|master
init|=
name|cluster
operator|.
name|getMaster
argument_list|()
decl_stmt|;
name|assertTrue
argument_list|(
literal|"Master should be active"
argument_list|,
name|master
operator|.
name|isActiveMaster
argument_list|()
argument_list|)
expr_stmt|;
name|MasterCoprocessorHost
name|host
init|=
name|master
operator|.
name|getCoprocessorHost
argument_list|()
decl_stmt|;
name|assertNotNull
argument_list|(
literal|"CoprocessorHost should not be null"
argument_list|,
name|host
argument_list|)
expr_stmt|;
name|CPMasterObserver
name|cp
init|=
operator|(
name|CPMasterObserver
operator|)
name|host
operator|.
name|findCoprocessor
argument_list|(
name|CPMasterObserver
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
name|assertNotNull
argument_list|(
literal|"CPMasterObserver coprocessor not found or not installed!"
argument_list|,
name|cp
argument_list|)
expr_stmt|;
comment|// check basic lifecycle
name|assertTrue
argument_list|(
literal|"MasterObserver should have been started"
argument_list|,
name|cp
operator|.
name|wasStarted
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
literal|"postStartMaster() hook should have been called"
argument_list|,
name|cp
operator|.
name|wasStartMasterCalled
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testTableOperations
parameter_list|()
throws|throws
name|Exception
block|{
name|MiniHBaseCluster
name|cluster
init|=
name|UTIL
operator|.
name|getHBaseCluster
argument_list|()
decl_stmt|;
name|HMaster
name|master
init|=
name|cluster
operator|.
name|getMaster
argument_list|()
decl_stmt|;
name|MasterCoprocessorHost
name|host
init|=
name|master
operator|.
name|getCoprocessorHost
argument_list|()
decl_stmt|;
name|CPMasterObserver
name|cp
init|=
operator|(
name|CPMasterObserver
operator|)
name|host
operator|.
name|findCoprocessor
argument_list|(
name|CPMasterObserver
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
name|assertFalse
argument_list|(
literal|"No table created yet"
argument_list|,
name|cp
operator|.
name|wasCreateTableCalled
argument_list|()
argument_list|)
expr_stmt|;
comment|// create a table
name|HTableDescriptor
name|htd
init|=
operator|new
name|HTableDescriptor
argument_list|(
name|TEST_TABLE
argument_list|)
decl_stmt|;
name|htd
operator|.
name|addFamily
argument_list|(
operator|new
name|HColumnDescriptor
argument_list|(
name|TEST_FAMILY
argument_list|)
argument_list|)
expr_stmt|;
name|HBaseAdmin
name|admin
init|=
name|UTIL
operator|.
name|getHBaseAdmin
argument_list|()
decl_stmt|;
name|admin
operator|.
name|createTable
argument_list|(
name|htd
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
literal|"Test table should be created"
argument_list|,
name|cp
operator|.
name|wasCreateTableCalled
argument_list|()
argument_list|)
expr_stmt|;
comment|// disable
name|assertFalse
argument_list|(
name|cp
operator|.
name|wasDisableTableCalled
argument_list|()
argument_list|)
expr_stmt|;
name|admin
operator|.
name|disableTable
argument_list|(
name|TEST_TABLE
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|admin
operator|.
name|isTableDisabled
argument_list|(
name|TEST_TABLE
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
literal|"Coprocessor should have been called on table disable"
argument_list|,
name|cp
operator|.
name|wasDisableTableCalled
argument_list|()
argument_list|)
expr_stmt|;
comment|// modify table
name|htd
operator|.
name|setMaxFileSize
argument_list|(
literal|512
operator|*
literal|1024
operator|*
literal|1024
argument_list|)
expr_stmt|;
name|admin
operator|.
name|modifyTable
argument_list|(
name|TEST_TABLE
argument_list|,
name|htd
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
literal|"Test table should have been modified"
argument_list|,
name|cp
operator|.
name|wasModifyTableCalled
argument_list|()
argument_list|)
expr_stmt|;
comment|// add a column family
name|admin
operator|.
name|addColumn
argument_list|(
name|TEST_TABLE
argument_list|,
operator|new
name|HColumnDescriptor
argument_list|(
name|TEST_FAMILY2
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
literal|"New column family should have been added to test table"
argument_list|,
name|cp
operator|.
name|wasAddColumnCalled
argument_list|()
argument_list|)
expr_stmt|;
comment|// modify a column family
name|HColumnDescriptor
name|hcd
init|=
operator|new
name|HColumnDescriptor
argument_list|(
name|TEST_FAMILY2
argument_list|)
decl_stmt|;
name|hcd
operator|.
name|setMaxVersions
argument_list|(
literal|25
argument_list|)
expr_stmt|;
name|admin
operator|.
name|modifyColumn
argument_list|(
name|TEST_TABLE
argument_list|,
name|hcd
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
literal|"Second column family should be modified"
argument_list|,
name|cp
operator|.
name|wasModifyColumnCalled
argument_list|()
argument_list|)
expr_stmt|;
comment|// enable
name|assertFalse
argument_list|(
name|cp
operator|.
name|wasEnableTableCalled
argument_list|()
argument_list|)
expr_stmt|;
name|admin
operator|.
name|enableTable
argument_list|(
name|TEST_TABLE
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|admin
operator|.
name|isTableEnabled
argument_list|(
name|TEST_TABLE
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
literal|"Coprocessor should have been called on table enable"
argument_list|,
name|cp
operator|.
name|wasEnableTableCalled
argument_list|()
argument_list|)
expr_stmt|;
comment|// disable again
name|admin
operator|.
name|disableTable
argument_list|(
name|TEST_TABLE
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|admin
operator|.
name|isTableDisabled
argument_list|(
name|TEST_TABLE
argument_list|)
argument_list|)
expr_stmt|;
comment|// delete column
name|assertFalse
argument_list|(
literal|"No column family deleted yet"
argument_list|,
name|cp
operator|.
name|wasDeleteColumnCalled
argument_list|()
argument_list|)
expr_stmt|;
name|admin
operator|.
name|deleteColumn
argument_list|(
name|TEST_TABLE
argument_list|,
name|TEST_FAMILY2
argument_list|)
expr_stmt|;
name|HTableDescriptor
name|tableDesc
init|=
name|admin
operator|.
name|getTableDescriptor
argument_list|(
name|TEST_TABLE
argument_list|)
decl_stmt|;
name|assertNull
argument_list|(
literal|"'"
operator|+
name|Bytes
operator|.
name|toString
argument_list|(
name|TEST_FAMILY2
argument_list|)
operator|+
literal|"' should have been removed"
argument_list|,
name|tableDesc
operator|.
name|getFamily
argument_list|(
name|TEST_FAMILY2
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
literal|"Coprocessor should have been called on column delete"
argument_list|,
name|cp
operator|.
name|wasDeleteColumnCalled
argument_list|()
argument_list|)
expr_stmt|;
comment|// delete table
name|assertFalse
argument_list|(
literal|"No table deleted yet"
argument_list|,
name|cp
operator|.
name|wasDeleteTableCalled
argument_list|()
argument_list|)
expr_stmt|;
name|admin
operator|.
name|deleteTable
argument_list|(
name|TEST_TABLE
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
literal|"Test table should have been deleted"
argument_list|,
name|admin
operator|.
name|tableExists
argument_list|(
name|TEST_TABLE
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
literal|"Coprocessor should have been called on table delete"
argument_list|,
name|cp
operator|.
name|wasDeleteTableCalled
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testRegionTransitionOperations
parameter_list|()
throws|throws
name|Exception
block|{
name|MiniHBaseCluster
name|cluster
init|=
name|UTIL
operator|.
name|getHBaseCluster
argument_list|()
decl_stmt|;
name|HMaster
name|master
init|=
name|cluster
operator|.
name|getMaster
argument_list|()
decl_stmt|;
name|MasterCoprocessorHost
name|host
init|=
name|master
operator|.
name|getCoprocessorHost
argument_list|()
decl_stmt|;
name|CPMasterObserver
name|cp
init|=
operator|(
name|CPMasterObserver
operator|)
name|host
operator|.
name|findCoprocessor
argument_list|(
name|CPMasterObserver
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
name|HTable
name|table
init|=
name|UTIL
operator|.
name|createTable
argument_list|(
name|TEST_TABLE
argument_list|,
name|TEST_FAMILY
argument_list|)
decl_stmt|;
name|UTIL
operator|.
name|createMultiRegions
argument_list|(
name|table
argument_list|,
name|TEST_FAMILY
argument_list|)
expr_stmt|;
name|Map
argument_list|<
name|HRegionInfo
argument_list|,
name|HServerAddress
argument_list|>
name|regions
init|=
name|table
operator|.
name|getRegionsInfo
argument_list|()
decl_stmt|;
name|assertFalse
argument_list|(
name|regions
operator|.
name|isEmpty
argument_list|()
argument_list|)
expr_stmt|;
name|Map
operator|.
name|Entry
argument_list|<
name|HRegionInfo
argument_list|,
name|HServerAddress
argument_list|>
name|firstRegion
init|=
name|regions
operator|.
name|entrySet
argument_list|()
operator|.
name|iterator
argument_list|()
operator|.
name|next
argument_list|()
decl_stmt|;
comment|// try to force a move
name|Collection
argument_list|<
name|ServerName
argument_list|>
name|servers
init|=
name|master
operator|.
name|getClusterStatus
argument_list|()
operator|.
name|getServers
argument_list|()
decl_stmt|;
name|String
name|destName
init|=
literal|null
decl_stmt|;
for|for
control|(
name|ServerName
name|info
range|:
name|servers
control|)
block|{
name|HServerAddress
name|hsa
init|=
operator|new
name|HServerAddress
argument_list|(
name|info
operator|.
name|getHostname
argument_list|()
argument_list|,
name|info
operator|.
name|getPort
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|hsa
operator|.
name|equals
argument_list|(
name|firstRegion
operator|.
name|getValue
argument_list|()
argument_list|)
condition|)
block|{
name|destName
operator|=
name|info
operator|.
name|toString
argument_list|()
expr_stmt|;
break|break;
block|}
block|}
name|master
operator|.
name|move
argument_list|(
name|firstRegion
operator|.
name|getKey
argument_list|()
operator|.
name|getEncodedNameAsBytes
argument_list|()
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
name|destName
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
literal|"Coprocessor should have been called on region move"
argument_list|,
name|cp
operator|.
name|wasMoveCalled
argument_list|()
argument_list|)
expr_stmt|;
comment|// make sure balancer is on
name|master
operator|.
name|balanceSwitch
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
literal|"Coprocessor should have been called on balance switch"
argument_list|,
name|cp
operator|.
name|wasBalanceSwitchCalled
argument_list|()
argument_list|)
expr_stmt|;
comment|// force region rebalancing
name|master
operator|.
name|balanceSwitch
argument_list|(
literal|false
argument_list|)
expr_stmt|;
comment|// move half the open regions from RS 0 to RS 1
name|HRegionServer
name|rs
init|=
name|cluster
operator|.
name|getRegionServer
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|byte
index|[]
name|destRS
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
name|cluster
operator|.
name|getRegionServer
argument_list|(
literal|1
argument_list|)
operator|.
name|getServerName
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|HRegionInfo
argument_list|>
name|openRegions
init|=
name|rs
operator|.
name|getOnlineRegions
argument_list|()
decl_stmt|;
name|int
name|moveCnt
init|=
name|openRegions
operator|.
name|size
argument_list|()
operator|/
literal|2
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
name|moveCnt
condition|;
name|i
operator|++
control|)
block|{
name|HRegionInfo
name|info
init|=
name|openRegions
operator|.
name|get
argument_list|(
name|i
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
operator|(
name|info
operator|.
name|isMetaRegion
argument_list|()
operator|||
name|info
operator|.
name|isRootRegion
argument_list|()
operator|)
condition|)
block|{
name|master
operator|.
name|move
argument_list|(
name|openRegions
operator|.
name|get
argument_list|(
name|i
argument_list|)
operator|.
name|getEncodedNameAsBytes
argument_list|()
argument_list|,
name|destRS
argument_list|)
expr_stmt|;
block|}
block|}
comment|// wait for assignments to finish
name|AssignmentManager
name|mgr
init|=
name|master
operator|.
name|getAssignmentManager
argument_list|()
decl_stmt|;
name|Collection
argument_list|<
name|AssignmentManager
operator|.
name|RegionState
argument_list|>
name|transRegions
init|=
name|mgr
operator|.
name|getRegionsInTransition
argument_list|()
operator|.
name|values
argument_list|()
decl_stmt|;
for|for
control|(
name|AssignmentManager
operator|.
name|RegionState
name|state
range|:
name|transRegions
control|)
block|{
name|mgr
operator|.
name|waitOnRegionToClearRegionsInTransition
argument_list|(
name|state
operator|.
name|getRegion
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|// now trigger a balance
name|master
operator|.
name|balanceSwitch
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|boolean
name|balanceRun
init|=
name|master
operator|.
name|balance
argument_list|()
decl_stmt|;
name|assertTrue
argument_list|(
literal|"Coprocessor should be called on region rebalancing"
argument_list|,
name|cp
operator|.
name|wasBalanceCalled
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

