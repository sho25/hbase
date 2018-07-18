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
name|replication
operator|.
name|regionserver
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
name|fs
operator|.
name|Path
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

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
specifier|final
class|class
name|ReplicationStatus
block|{
specifier|private
specifier|final
name|String
name|peerId
decl_stmt|;
specifier|private
specifier|final
name|String
name|walGroup
decl_stmt|;
specifier|private
specifier|final
name|Path
name|currentPath
decl_stmt|;
specifier|private
specifier|final
name|int
name|queueSize
decl_stmt|;
specifier|private
specifier|final
name|long
name|ageOfLastShippedOp
decl_stmt|;
specifier|private
specifier|final
name|long
name|replicationDelay
decl_stmt|;
specifier|private
specifier|final
name|long
name|currentPosition
decl_stmt|;
specifier|private
specifier|final
name|long
name|fileSize
decl_stmt|;
specifier|private
name|ReplicationStatus
parameter_list|(
name|ReplicationStatusBuilder
name|builder
parameter_list|)
block|{
name|this
operator|.
name|peerId
operator|=
name|builder
operator|.
name|peerId
expr_stmt|;
name|this
operator|.
name|walGroup
operator|=
name|builder
operator|.
name|walGroup
expr_stmt|;
name|this
operator|.
name|currentPath
operator|=
name|builder
operator|.
name|currentPath
expr_stmt|;
name|this
operator|.
name|queueSize
operator|=
name|builder
operator|.
name|queueSize
expr_stmt|;
name|this
operator|.
name|ageOfLastShippedOp
operator|=
name|builder
operator|.
name|ageOfLastShippedOp
expr_stmt|;
name|this
operator|.
name|replicationDelay
operator|=
name|builder
operator|.
name|replicationDelay
expr_stmt|;
name|this
operator|.
name|currentPosition
operator|=
name|builder
operator|.
name|currentPosition
expr_stmt|;
name|this
operator|.
name|fileSize
operator|=
name|builder
operator|.
name|fileSize
expr_stmt|;
block|}
specifier|public
name|long
name|getCurrentPosition
parameter_list|()
block|{
return|return
name|currentPosition
return|;
block|}
specifier|public
name|long
name|getFileSize
parameter_list|()
block|{
return|return
name|fileSize
return|;
block|}
specifier|public
name|String
name|getPeerId
parameter_list|()
block|{
return|return
name|peerId
return|;
block|}
specifier|public
name|String
name|getWalGroup
parameter_list|()
block|{
return|return
name|walGroup
return|;
block|}
specifier|public
name|int
name|getQueueSize
parameter_list|()
block|{
return|return
name|queueSize
return|;
block|}
specifier|public
name|long
name|getAgeOfLastShippedOp
parameter_list|()
block|{
return|return
name|ageOfLastShippedOp
return|;
block|}
specifier|public
name|long
name|getReplicationDelay
parameter_list|()
block|{
return|return
name|replicationDelay
return|;
block|}
specifier|public
name|Path
name|getCurrentPath
parameter_list|()
block|{
return|return
name|currentPath
return|;
block|}
specifier|public
specifier|static
name|ReplicationStatusBuilder
name|newBuilder
parameter_list|()
block|{
return|return
operator|new
name|ReplicationStatusBuilder
argument_list|()
return|;
block|}
specifier|public
specifier|static
class|class
name|ReplicationStatusBuilder
block|{
specifier|private
name|String
name|peerId
init|=
literal|"UNKNOWN"
decl_stmt|;
specifier|private
name|String
name|walGroup
init|=
literal|"UNKNOWN"
decl_stmt|;
specifier|private
name|Path
name|currentPath
init|=
operator|new
name|Path
argument_list|(
literal|"UNKNOWN"
argument_list|)
decl_stmt|;
specifier|private
name|int
name|queueSize
init|=
operator|-
literal|1
decl_stmt|;
specifier|private
name|long
name|ageOfLastShippedOp
init|=
operator|-
literal|1
decl_stmt|;
specifier|private
name|long
name|replicationDelay
init|=
operator|-
literal|1
decl_stmt|;
specifier|private
name|long
name|currentPosition
init|=
operator|-
literal|1
decl_stmt|;
specifier|private
name|long
name|fileSize
init|=
operator|-
literal|1
decl_stmt|;
specifier|public
name|ReplicationStatusBuilder
name|withPeerId
parameter_list|(
name|String
name|peerId
parameter_list|)
block|{
name|this
operator|.
name|peerId
operator|=
name|peerId
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|ReplicationStatusBuilder
name|withFileSize
parameter_list|(
name|long
name|fileSize
parameter_list|)
block|{
name|this
operator|.
name|fileSize
operator|=
name|fileSize
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|ReplicationStatusBuilder
name|withWalGroup
parameter_list|(
name|String
name|walGroup
parameter_list|)
block|{
name|this
operator|.
name|walGroup
operator|=
name|walGroup
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|ReplicationStatusBuilder
name|withCurrentPath
parameter_list|(
name|Path
name|currentPath
parameter_list|)
block|{
name|this
operator|.
name|currentPath
operator|=
name|currentPath
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|ReplicationStatusBuilder
name|withQueueSize
parameter_list|(
name|int
name|queueSize
parameter_list|)
block|{
name|this
operator|.
name|queueSize
operator|=
name|queueSize
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|ReplicationStatusBuilder
name|withAgeOfLastShippedOp
parameter_list|(
name|long
name|ageOfLastShippedOp
parameter_list|)
block|{
name|this
operator|.
name|ageOfLastShippedOp
operator|=
name|ageOfLastShippedOp
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|ReplicationStatusBuilder
name|withReplicationDelay
parameter_list|(
name|long
name|replicationDelay
parameter_list|)
block|{
name|this
operator|.
name|replicationDelay
operator|=
name|replicationDelay
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|ReplicationStatusBuilder
name|withCurrentPosition
parameter_list|(
name|long
name|currentPosition
parameter_list|)
block|{
name|this
operator|.
name|currentPosition
operator|=
name|currentPosition
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|ReplicationStatus
name|build
parameter_list|()
block|{
return|return
operator|new
name|ReplicationStatus
argument_list|(
name|this
argument_list|)
return|;
block|}
block|}
block|}
end_class

end_unit
