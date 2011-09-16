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
name|util
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
name|NotServingRegionException
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
name|ZooKeeperConnectionException
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
name|HConnection
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
name|HConnectionManager
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
name|HRegionInterface
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

begin_class
specifier|public
class|class
name|HBaseFsckRepair
block|{
comment|/**    * Fix dupe assignment by doing silent closes on each RS hosting the region    * and then force ZK unassigned node to OFFLINE to trigger assignment by    * master.    * @param admin    * @param region    * @param servers    * @throws IOException    * @throws KeeperException    * @throws InterruptedException    */
specifier|public
specifier|static
name|void
name|fixDupeAssignment
parameter_list|(
name|HBaseAdmin
name|admin
parameter_list|,
name|HRegionInfo
name|region
parameter_list|,
name|List
argument_list|<
name|ServerName
argument_list|>
name|servers
parameter_list|)
throws|throws
name|IOException
throws|,
name|KeeperException
throws|,
name|InterruptedException
block|{
name|HRegionInfo
name|actualRegion
init|=
operator|new
name|HRegionInfo
argument_list|(
name|region
argument_list|)
decl_stmt|;
comment|// Close region on the servers silently
for|for
control|(
name|ServerName
name|server
range|:
name|servers
control|)
block|{
name|closeRegionSilentlyAndWait
argument_list|(
name|admin
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|server
argument_list|,
name|actualRegion
argument_list|)
expr_stmt|;
block|}
comment|// Force ZK node to OFFLINE so master assigns
name|forceOfflineInZK
argument_list|(
name|admin
argument_list|,
name|actualRegion
argument_list|)
expr_stmt|;
block|}
comment|/**    * Fix unassigned by creating/transition the unassigned ZK node for this    * region to OFFLINE state with a special flag to tell the master that this    * is a forced operation by HBCK.    * @param admin    * @param region    * @throws IOException    * @throws KeeperException    */
specifier|public
specifier|static
name|void
name|fixUnassigned
parameter_list|(
name|HBaseAdmin
name|admin
parameter_list|,
name|HRegionInfo
name|region
parameter_list|)
throws|throws
name|IOException
throws|,
name|KeeperException
block|{
name|HRegionInfo
name|actualRegion
init|=
operator|new
name|HRegionInfo
argument_list|(
name|region
argument_list|)
decl_stmt|;
comment|// Force ZK node to OFFLINE so master assigns
name|forceOfflineInZK
argument_list|(
name|admin
argument_list|,
name|actualRegion
argument_list|)
expr_stmt|;
block|}
specifier|private
specifier|static
name|void
name|forceOfflineInZK
parameter_list|(
name|HBaseAdmin
name|admin
parameter_list|,
specifier|final
name|HRegionInfo
name|region
parameter_list|)
throws|throws
name|ZooKeeperConnectionException
throws|,
name|KeeperException
throws|,
name|IOException
block|{
name|admin
operator|.
name|assign
argument_list|(
name|region
operator|.
name|getRegionName
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|private
specifier|static
name|void
name|closeRegionSilentlyAndWait
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|ServerName
name|server
parameter_list|,
name|HRegionInfo
name|region
parameter_list|)
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{
name|HConnection
name|connection
init|=
name|HConnectionManager
operator|.
name|getConnection
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|boolean
name|success
init|=
literal|false
decl_stmt|;
try|try
block|{
name|HRegionInterface
name|rs
init|=
name|connection
operator|.
name|getHRegionConnection
argument_list|(
name|server
operator|.
name|getHostname
argument_list|()
argument_list|,
name|server
operator|.
name|getPort
argument_list|()
argument_list|)
decl_stmt|;
name|rs
operator|.
name|closeRegion
argument_list|(
name|region
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|long
name|timeout
init|=
name|conf
operator|.
name|getLong
argument_list|(
literal|"hbase.hbck.close.timeout"
argument_list|,
literal|120000
argument_list|)
decl_stmt|;
name|long
name|expiration
init|=
name|timeout
operator|+
name|System
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
while|while
condition|(
name|System
operator|.
name|currentTimeMillis
argument_list|()
operator|<
name|expiration
condition|)
block|{
try|try
block|{
name|HRegionInfo
name|rsRegion
init|=
name|rs
operator|.
name|getRegionInfo
argument_list|(
name|region
operator|.
name|getRegionName
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|rsRegion
operator|==
literal|null
condition|)
throw|throw
operator|new
name|NotServingRegionException
argument_list|()
throw|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|success
operator|=
literal|true
expr_stmt|;
return|return;
block|}
name|Thread
operator|.
name|sleep
argument_list|(
literal|1000
argument_list|)
expr_stmt|;
block|}
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Region "
operator|+
name|region
operator|+
literal|" failed to close within"
operator|+
literal|" timeout "
operator|+
name|timeout
argument_list|)
throw|;
block|}
finally|finally
block|{
try|try
block|{
name|connection
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|ioe
parameter_list|)
block|{
if|if
condition|(
name|success
condition|)
block|{
throw|throw
name|ioe
throw|;
block|}
block|}
block|}
block|}
block|}
end_class

end_unit

