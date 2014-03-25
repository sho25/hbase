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
name|master
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
name|assertNotSame
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
name|exceptions
operator|.
name|DeserializationException
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
name|RegionTransition
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
name|executor
operator|.
name|EventType
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
name|RegionState
operator|.
name|State
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
name|zookeeper
operator|.
name|ZKAssign
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
name|zookeeper
operator|.
name|ZKUtil
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
name|zookeeper
operator|.
name|ZooKeeperWatcher
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|zookeeper
operator|.
name|KeeperException
import|;
end_import

begin_comment
comment|/**  * Package scoped mocking utility.  */
end_comment

begin_class
specifier|public
class|class
name|Mocking
block|{
specifier|static
name|void
name|waitForRegionFailedToCloseAndSetToPendingClose
parameter_list|(
name|AssignmentManager
name|am
parameter_list|,
name|HRegionInfo
name|hri
parameter_list|)
throws|throws
name|InterruptedException
block|{
comment|// Since region server is fake, sendRegionClose will fail, and closing
comment|// region will fail. For testing purpose, moving it back to pending close
name|boolean
name|wait
init|=
literal|true
decl_stmt|;
while|while
condition|(
name|wait
condition|)
block|{
name|RegionState
name|state
init|=
name|am
operator|.
name|getRegionStates
argument_list|()
operator|.
name|getRegionState
argument_list|(
name|hri
argument_list|)
decl_stmt|;
if|if
condition|(
name|state
operator|!=
literal|null
operator|&&
name|state
operator|.
name|isFailedClose
argument_list|()
condition|)
block|{
name|am
operator|.
name|getRegionStates
argument_list|()
operator|.
name|updateRegionState
argument_list|(
name|hri
argument_list|,
name|State
operator|.
name|PENDING_CLOSE
argument_list|)
expr_stmt|;
name|wait
operator|=
literal|false
expr_stmt|;
block|}
else|else
block|{
name|Thread
operator|.
name|sleep
argument_list|(
literal|1
argument_list|)
expr_stmt|;
block|}
block|}
block|}
specifier|static
name|void
name|waitForRegionPendingOpenInRIT
parameter_list|(
name|AssignmentManager
name|am
parameter_list|,
name|String
name|encodedName
parameter_list|)
throws|throws
name|InterruptedException
block|{
comment|// We used to do a check like this:
comment|//!Mocking.verifyRegionState(this.watcher, REGIONINFO, EventType.M_ZK_REGION_OFFLINE)) {
comment|// There is a race condition with this: because we may do the transition to
comment|// RS_ZK_REGION_OPENING before the RIT is internally updated. We need to wait for the
comment|// RIT to be as we need it to be instead. This cannot happen in a real cluster as we
comment|// update the RIT before sending the openRegion request.
name|boolean
name|wait
init|=
literal|true
decl_stmt|;
while|while
condition|(
name|wait
condition|)
block|{
name|RegionState
name|state
init|=
name|am
operator|.
name|getRegionStates
argument_list|()
operator|.
name|getRegionsInTransition
argument_list|()
operator|.
name|get
argument_list|(
name|encodedName
argument_list|)
decl_stmt|;
if|if
condition|(
name|state
operator|!=
literal|null
operator|&&
name|state
operator|.
name|isPendingOpen
argument_list|()
condition|)
block|{
name|wait
operator|=
literal|false
expr_stmt|;
block|}
else|else
block|{
name|Thread
operator|.
name|sleep
argument_list|(
literal|1
argument_list|)
expr_stmt|;
block|}
block|}
block|}
comment|/**    * Verifies that the specified region is in the specified state in ZooKeeper.    *<p>    * Returns true if region is in transition and in the specified state in    * ZooKeeper.  Returns false if the region does not exist in ZK or is in    * a different state.    *<p>    * Method synchronizes() with ZK so will yield an up-to-date result but is    * a slow read.    * @param zkw    * @param region    * @param expectedState    * @return true if region exists and is in expected state    * @throws DeserializationException    */
specifier|static
name|boolean
name|verifyRegionState
parameter_list|(
name|ZooKeeperWatcher
name|zkw
parameter_list|,
name|HRegionInfo
name|region
parameter_list|,
name|EventType
name|expectedState
parameter_list|)
throws|throws
name|KeeperException
throws|,
name|DeserializationException
block|{
name|String
name|encoded
init|=
name|region
operator|.
name|getEncodedName
argument_list|()
decl_stmt|;
name|String
name|node
init|=
name|ZKAssign
operator|.
name|getNodeName
argument_list|(
name|zkw
argument_list|,
name|encoded
argument_list|)
decl_stmt|;
name|zkw
operator|.
name|sync
argument_list|(
name|node
argument_list|)
expr_stmt|;
comment|// Read existing data of the node
name|byte
index|[]
name|existingBytes
init|=
literal|null
decl_stmt|;
try|try
block|{
name|existingBytes
operator|=
name|ZKUtil
operator|.
name|getDataAndWatch
argument_list|(
name|zkw
argument_list|,
name|node
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|KeeperException
operator|.
name|NoNodeException
name|nne
parameter_list|)
block|{
return|return
literal|false
return|;
block|}
catch|catch
parameter_list|(
name|KeeperException
name|e
parameter_list|)
block|{
throw|throw
name|e
throw|;
block|}
if|if
condition|(
name|existingBytes
operator|==
literal|null
condition|)
return|return
literal|false
return|;
name|RegionTransition
name|rt
init|=
name|RegionTransition
operator|.
name|parseFrom
argument_list|(
name|existingBytes
argument_list|)
decl_stmt|;
return|return
name|rt
operator|.
name|getEventType
argument_list|()
operator|.
name|equals
argument_list|(
name|expectedState
argument_list|)
return|;
block|}
block|}
end_class

end_unit

