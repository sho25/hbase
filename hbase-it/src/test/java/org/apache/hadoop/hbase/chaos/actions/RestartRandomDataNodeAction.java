begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *<p>  * http://www.apache.org/licenses/LICENSE-2.0  *<p>  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|chaos
operator|.
name|actions
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
name|chaos
operator|.
name|monkies
operator|.
name|PolicyBasedChaosMonkey
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
name|FSUtils
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
name|hdfs
operator|.
name|DFSClient
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
name|hdfs
operator|.
name|DistributedFileSystem
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
name|hdfs
operator|.
name|protocol
operator|.
name|DatanodeInfo
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
name|hdfs
operator|.
name|protocol
operator|.
name|HdfsConstants
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
name|LinkedList
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

begin_comment
comment|/**  * Action that restarts a random datanode.  */
end_comment

begin_class
specifier|public
class|class
name|RestartRandomDataNodeAction
extends|extends
name|RestartActionBaseAction
block|{
specifier|public
name|RestartRandomDataNodeAction
parameter_list|(
name|long
name|sleepTime
parameter_list|)
block|{
name|super
argument_list|(
name|sleepTime
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|perform
parameter_list|()
throws|throws
name|Exception
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Performing action: Restart random data node"
argument_list|)
expr_stmt|;
name|ServerName
name|server
init|=
name|PolicyBasedChaosMonkey
operator|.
name|selectRandomItem
argument_list|(
name|getDataNodes
argument_list|()
argument_list|)
decl_stmt|;
name|restartDataNode
argument_list|(
name|server
argument_list|,
name|sleepTime
argument_list|)
expr_stmt|;
block|}
specifier|public
name|ServerName
index|[]
name|getDataNodes
parameter_list|()
throws|throws
name|IOException
block|{
name|DistributedFileSystem
name|fs
init|=
operator|(
name|DistributedFileSystem
operator|)
name|FSUtils
operator|.
name|getRootDir
argument_list|(
name|getConf
argument_list|()
argument_list|)
operator|.
name|getFileSystem
argument_list|(
name|getConf
argument_list|()
argument_list|)
decl_stmt|;
name|DFSClient
name|dfsClient
init|=
name|fs
operator|.
name|getClient
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|ServerName
argument_list|>
name|hosts
init|=
operator|new
name|LinkedList
argument_list|<>
argument_list|()
decl_stmt|;
for|for
control|(
name|DatanodeInfo
name|dataNode
range|:
name|dfsClient
operator|.
name|datanodeReport
argument_list|(
name|HdfsConstants
operator|.
name|DatanodeReportType
operator|.
name|LIVE
argument_list|)
control|)
block|{
name|hosts
operator|.
name|add
argument_list|(
name|ServerName
operator|.
name|valueOf
argument_list|(
name|dataNode
operator|.
name|getHostName
argument_list|()
argument_list|,
operator|-
literal|1
argument_list|,
operator|-
literal|1
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|hosts
operator|.
name|toArray
argument_list|(
operator|new
name|ServerName
index|[
name|hosts
operator|.
name|size
argument_list|()
index|]
argument_list|)
return|;
block|}
block|}
end_class

end_unit

