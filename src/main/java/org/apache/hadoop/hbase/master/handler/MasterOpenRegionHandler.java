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
name|master
operator|.
name|handler
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
name|HMsg
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
name|HServerInfo
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
name|RegionTransitionEventData
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
name|HBaseEventHandler
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
name|util
operator|.
name|Writables
import|;
end_import

begin_comment
comment|/**  * This is the event handler for all events relating to opening regions on the  * HMaster. This could be one of the following:  *   - notification that a region server is "OPENING" a region  *   - notification that a region server has "OPENED" a region  * The following event types map to this handler:  *   - RS_REGION_OPENING  *   - RS_REGION_OPENED  */
end_comment

begin_class
specifier|public
class|class
name|MasterOpenRegionHandler
extends|extends
name|HBaseEventHandler
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
name|MasterOpenRegionHandler
operator|.
name|class
argument_list|)
decl_stmt|;
comment|// other args passed in a byte array form
specifier|protected
name|byte
index|[]
name|serializedData
decl_stmt|;
specifier|private
name|String
name|regionName
decl_stmt|;
specifier|private
name|RegionTransitionEventData
name|hbEventData
decl_stmt|;
name|ServerManager
name|serverManager
decl_stmt|;
specifier|public
name|MasterOpenRegionHandler
parameter_list|(
name|HBaseEventType
name|eventType
parameter_list|,
name|ServerManager
name|serverManager
parameter_list|,
name|String
name|serverName
parameter_list|,
name|String
name|regionName
parameter_list|,
name|byte
index|[]
name|serData
parameter_list|)
block|{
name|super
argument_list|(
literal|false
argument_list|,
name|serverName
argument_list|,
name|eventType
argument_list|)
expr_stmt|;
name|this
operator|.
name|regionName
operator|=
name|regionName
expr_stmt|;
name|this
operator|.
name|serializedData
operator|=
name|serData
expr_stmt|;
name|this
operator|.
name|serverManager
operator|=
name|serverManager
expr_stmt|;
block|}
comment|/**    * Handle the various events relating to opening regions. We can get the     * following events here:    *   - RS_REGION_OPENING : Keep track to see how long the region open takes.     *                         If the RS is taking too long, then revert the     *                         region back to closed state so that it can be     *                         re-assigned.    *   - RS_REGION_OPENED  : The region is opened. Add an entry into META for      *                         the RS having opened this region. Then delete this     *                         entry in ZK.    */
annotation|@
name|Override
specifier|public
name|void
name|process
parameter_list|()
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Event = "
operator|+
name|getHBEvent
argument_list|()
operator|+
literal|", region = "
operator|+
name|regionName
argument_list|)
expr_stmt|;
if|if
condition|(
name|this
operator|.
name|getHBEvent
argument_list|()
operator|==
name|HBaseEventType
operator|.
name|RS2ZK_REGION_OPENING
condition|)
block|{
name|handleRegionOpeningEvent
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|this
operator|.
name|getHBEvent
argument_list|()
operator|==
name|HBaseEventType
operator|.
name|RS2ZK_REGION_OPENED
condition|)
block|{
name|handleRegionOpenedEvent
argument_list|()
expr_stmt|;
block|}
block|}
specifier|private
name|void
name|handleRegionOpeningEvent
parameter_list|()
block|{
comment|// TODO: not implemented.
name|LOG
operator|.
name|debug
argument_list|(
literal|"NO-OP call to handling region opening event"
argument_list|)
expr_stmt|;
comment|// Keep track to see how long the region open takes. If the RS is taking too
comment|// long, then revert the region back to closed state so that it can be
comment|// re-assigned.
block|}
specifier|private
name|void
name|handleRegionOpenedEvent
parameter_list|()
block|{
try|try
block|{
if|if
condition|(
name|hbEventData
operator|==
literal|null
condition|)
block|{
name|hbEventData
operator|=
operator|new
name|RegionTransitionEventData
argument_list|()
expr_stmt|;
name|Writables
operator|.
name|getWritable
argument_list|(
name|serializedData
argument_list|,
name|hbEventData
argument_list|)
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Could not deserialize additional args for Open region"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
name|LOG
operator|.
name|debug
argument_list|(
literal|"RS "
operator|+
name|hbEventData
operator|.
name|getRsName
argument_list|()
operator|+
literal|" has opened region "
operator|+
name|regionName
argument_list|)
expr_stmt|;
name|HServerInfo
name|serverInfo
init|=
name|serverManager
operator|.
name|getServerInfo
argument_list|(
name|hbEventData
operator|.
name|getRsName
argument_list|()
argument_list|)
decl_stmt|;
name|ArrayList
argument_list|<
name|HMsg
argument_list|>
name|returnMsgs
init|=
operator|new
name|ArrayList
argument_list|<
name|HMsg
argument_list|>
argument_list|()
decl_stmt|;
name|serverManager
operator|.
name|processRegionOpen
argument_list|(
name|serverInfo
argument_list|,
name|hbEventData
operator|.
name|getHmsg
argument_list|()
operator|.
name|getRegionInfo
argument_list|()
argument_list|,
name|returnMsgs
argument_list|)
expr_stmt|;
if|if
condition|(
name|returnMsgs
operator|.
name|size
argument_list|()
operator|>
literal|0
condition|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Open region tried to send message: "
operator|+
name|returnMsgs
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getType
argument_list|()
operator|+
literal|" about "
operator|+
name|returnMsgs
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getRegionInfo
argument_list|()
operator|.
name|getRegionNameAsString
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

