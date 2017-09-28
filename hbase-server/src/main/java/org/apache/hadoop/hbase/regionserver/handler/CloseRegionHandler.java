begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|executor
operator|.
name|EventHandler
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
name|regionserver
operator|.
name|HRegion
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
name|RegionServerServices
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|yetus
operator|.
name|audience
operator|.
name|InterfaceAudience
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
name|RegionServerStatusProtos
operator|.
name|RegionStateTransition
operator|.
name|TransitionCode
import|;
end_import

begin_comment
comment|/**  * Handles closing of a region on a region server.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|CloseRegionHandler
extends|extends
name|EventHandler
block|{
comment|// NOTE on priorities shutting down.  There are none for close. There are some
comment|// for open.  I think that is right.  On shutdown, we want the meta to close
comment|// after the user regions have closed.  What
comment|// about the case where master tells us to shutdown a catalog region and we
comment|// have a running queue of user regions to close?
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
name|CloseRegionHandler
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|RegionServerServices
name|rsServices
decl_stmt|;
specifier|private
specifier|final
name|RegionInfo
name|regionInfo
decl_stmt|;
comment|// If true, the hosting server is aborting.  Region close process is different
comment|// when we are aborting.
specifier|private
specifier|final
name|boolean
name|abort
decl_stmt|;
specifier|private
name|ServerName
name|destination
decl_stmt|;
comment|/**    * This method used internally by the RegionServer to close out regions.    * @param server    * @param rsServices    * @param regionInfo    * @param abort If the regionserver is aborting.    * @param destination    */
specifier|public
name|CloseRegionHandler
parameter_list|(
specifier|final
name|Server
name|server
parameter_list|,
specifier|final
name|RegionServerServices
name|rsServices
parameter_list|,
specifier|final
name|RegionInfo
name|regionInfo
parameter_list|,
specifier|final
name|boolean
name|abort
parameter_list|,
name|ServerName
name|destination
parameter_list|)
block|{
name|this
argument_list|(
name|server
argument_list|,
name|rsServices
argument_list|,
name|regionInfo
argument_list|,
name|abort
argument_list|,
name|EventType
operator|.
name|M_RS_CLOSE_REGION
argument_list|,
name|destination
argument_list|)
expr_stmt|;
block|}
specifier|protected
name|CloseRegionHandler
parameter_list|(
specifier|final
name|Server
name|server
parameter_list|,
specifier|final
name|RegionServerServices
name|rsServices
parameter_list|,
name|RegionInfo
name|regionInfo
parameter_list|,
name|boolean
name|abort
parameter_list|,
name|EventType
name|eventType
parameter_list|,
name|ServerName
name|destination
parameter_list|)
block|{
name|super
argument_list|(
name|server
argument_list|,
name|eventType
argument_list|)
expr_stmt|;
name|this
operator|.
name|server
operator|=
name|server
expr_stmt|;
name|this
operator|.
name|rsServices
operator|=
name|rsServices
expr_stmt|;
name|this
operator|.
name|regionInfo
operator|=
name|regionInfo
expr_stmt|;
name|this
operator|.
name|abort
operator|=
name|abort
expr_stmt|;
name|this
operator|.
name|destination
operator|=
name|destination
expr_stmt|;
block|}
specifier|public
name|RegionInfo
name|getRegionInfo
parameter_list|()
block|{
return|return
name|regionInfo
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|process
parameter_list|()
block|{
try|try
block|{
name|String
name|name
init|=
name|regionInfo
operator|.
name|getRegionNameAsString
argument_list|()
decl_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"Processing close of "
operator|+
name|name
argument_list|)
expr_stmt|;
name|String
name|encodedRegionName
init|=
name|regionInfo
operator|.
name|getEncodedName
argument_list|()
decl_stmt|;
comment|// Check that this region is being served here
name|HRegion
name|region
init|=
operator|(
name|HRegion
operator|)
name|rsServices
operator|.
name|getRegion
argument_list|(
name|encodedRegionName
argument_list|)
decl_stmt|;
if|if
condition|(
name|region
operator|==
literal|null
condition|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Received CLOSE for region "
operator|+
name|name
operator|+
literal|" but currently not serving - ignoring"
argument_list|)
expr_stmt|;
comment|// TODO: do better than a simple warning
return|return;
block|}
comment|// Close the region
try|try
block|{
if|if
condition|(
name|region
operator|.
name|close
argument_list|(
name|abort
argument_list|)
operator|==
literal|null
condition|)
block|{
comment|// This region got closed.  Most likely due to a split.
comment|// The split message will clean up the master state.
name|LOG
operator|.
name|warn
argument_list|(
literal|"Can't close region: was already closed during close(): "
operator|+
name|name
argument_list|)
expr_stmt|;
return|return;
block|}
block|}
catch|catch
parameter_list|(
name|IOException
name|ioe
parameter_list|)
block|{
comment|// An IOException here indicates that we couldn't successfully flush the
comment|// memstore before closing. So, we need to abort the server and allow
comment|// the master to split our logs in order to recover the data.
name|server
operator|.
name|abort
argument_list|(
literal|"Unrecoverable exception while closing region "
operator|+
name|regionInfo
operator|.
name|getRegionNameAsString
argument_list|()
operator|+
literal|", still finishing close"
argument_list|,
name|ioe
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|RuntimeException
argument_list|(
name|ioe
argument_list|)
throw|;
block|}
name|this
operator|.
name|rsServices
operator|.
name|removeRegion
argument_list|(
name|region
argument_list|,
name|destination
argument_list|)
expr_stmt|;
name|rsServices
operator|.
name|reportRegionStateTransition
argument_list|(
name|TransitionCode
operator|.
name|CLOSED
argument_list|,
name|regionInfo
argument_list|)
expr_stmt|;
comment|// Done!  Region is closed on this RS
name|LOG
operator|.
name|debug
argument_list|(
literal|"Closed "
operator|+
name|region
operator|.
name|getRegionInfo
argument_list|()
operator|.
name|getRegionNameAsString
argument_list|()
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|this
operator|.
name|rsServices
operator|.
name|getRegionsInTransitionInRS
argument_list|()
operator|.
name|remove
argument_list|(
name|this
operator|.
name|regionInfo
operator|.
name|getEncodedNameAsBytes
argument_list|()
argument_list|,
name|Boolean
operator|.
name|FALSE
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

