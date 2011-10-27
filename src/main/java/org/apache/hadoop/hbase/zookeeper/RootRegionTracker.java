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
name|zookeeper
package|;
end_package

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
name|Abortable
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
name|catalog
operator|.
name|RootLocationEditor
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
name|Addressing
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
name|zookeeper
operator|.
name|KeeperException
import|;
end_import

begin_comment
comment|/**  * Tracks the root region server location node in zookeeper.  * Root region location is set by {@link RootLocationEditor} usually called  * out of<code>RegionServerServices</code>.  * This class has a watcher on the root location and notices changes.  */
end_comment

begin_class
specifier|public
class|class
name|RootRegionTracker
extends|extends
name|ZooKeeperNodeTracker
block|{
comment|/**    * Creates a root region location tracker.    *    *<p>After construction, use {@link #start} to kick off tracking.    *    * @param watcher    * @param abortable    */
specifier|public
name|RootRegionTracker
parameter_list|(
name|ZooKeeperWatcher
name|watcher
parameter_list|,
name|Abortable
name|abortable
parameter_list|)
block|{
name|super
argument_list|(
name|watcher
argument_list|,
name|watcher
operator|.
name|rootServerZNode
argument_list|,
name|abortable
argument_list|)
expr_stmt|;
block|}
comment|/**    * Checks if the root region location is available.    * @return true if root region location is available, false if not    */
specifier|public
name|boolean
name|isLocationAvailable
parameter_list|()
block|{
return|return
name|super
operator|.
name|getData
argument_list|(
literal|true
argument_list|)
operator|!=
literal|null
return|;
block|}
comment|/**    * Gets the root region location, if available.  Null if not.  Does not block.    * @return server name    * @throws InterruptedException    */
specifier|public
name|ServerName
name|getRootRegionLocation
parameter_list|()
throws|throws
name|InterruptedException
block|{
return|return
name|dataToServerName
argument_list|(
name|super
operator|.
name|getData
argument_list|(
literal|true
argument_list|)
argument_list|)
return|;
block|}
comment|/**    * Gets the root region location, if available, and waits for up to the    * specified timeout if not immediately available.    * Given the zookeeper notification could be delayed, we will try to    * get the latest data.    * @param timeout maximum time to wait, in millis    * @return server name for server hosting root region formatted as per    * {@link ServerName}, or null if none available    * @throws InterruptedException if interrupted while waiting    */
specifier|public
name|ServerName
name|waitRootRegionLocation
parameter_list|(
name|long
name|timeout
parameter_list|)
throws|throws
name|InterruptedException
block|{
if|if
condition|(
literal|false
operator|==
name|checkIfBaseNodeAvailable
argument_list|()
condition|)
block|{
name|String
name|errorMsg
init|=
literal|"Check the value configured in 'zookeeper.znode.parent'. "
operator|+
literal|"There could be a mismatch with the one configured in the master."
decl_stmt|;
name|LOG
operator|.
name|error
argument_list|(
name|errorMsg
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
name|errorMsg
argument_list|)
throw|;
block|}
return|return
name|dataToServerName
argument_list|(
name|super
operator|.
name|blockUntilAvailable
argument_list|(
name|timeout
argument_list|,
literal|true
argument_list|)
argument_list|)
return|;
block|}
comment|/*    * @param data    * @return Returns null if<code>data</code> is null else converts passed data    * to a ServerName instance.    */
specifier|private
specifier|static
name|ServerName
name|dataToServerName
parameter_list|(
specifier|final
name|byte
index|[]
name|data
parameter_list|)
block|{
comment|// The str returned could be old style -- pre hbase-1502 -- which was
comment|// hostname and port seperated by a colon rather than hostname, port and
comment|// startcode delimited by a ','.
if|if
condition|(
name|data
operator|==
literal|null
operator|||
name|data
operator|.
name|length
operator|<=
literal|0
condition|)
return|return
literal|null
return|;
name|String
name|str
init|=
name|Bytes
operator|.
name|toString
argument_list|(
name|data
argument_list|)
decl_stmt|;
name|int
name|index
init|=
name|str
operator|.
name|indexOf
argument_list|(
name|ServerName
operator|.
name|SERVERNAME_SEPARATOR
argument_list|)
decl_stmt|;
if|if
condition|(
name|index
operator|!=
operator|-
literal|1
condition|)
block|{
comment|// Presume its ServerName.toString() format.
return|return
name|ServerName
operator|.
name|parseServerName
argument_list|(
name|str
argument_list|)
return|;
block|}
comment|// Presume it a hostname:port format.
name|String
name|hostname
init|=
name|Addressing
operator|.
name|parseHostname
argument_list|(
name|str
argument_list|)
decl_stmt|;
name|int
name|port
init|=
name|Addressing
operator|.
name|parsePort
argument_list|(
name|str
argument_list|)
decl_stmt|;
return|return
operator|new
name|ServerName
argument_list|(
name|hostname
argument_list|,
name|port
argument_list|,
operator|-
literal|1L
argument_list|)
return|;
block|}
block|}
end_class

end_unit

