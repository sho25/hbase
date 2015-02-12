begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Licensed to the Apache Software Foundation (ASF) under one or more contributor license  * agreements. See the NOTICE file distributed with this work for additional information regarding  * copyright ownership. The ASF licenses this file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance with the License. You may obtain a  * copy of the License at http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable  * law or agreed to in writing, software distributed under the License is distributed on an "AS IS"  * BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License  * for the specific language governing permissions and limitations under the License.  */
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
name|classification
operator|.
name|InterfaceAudience
import|;
end_import

begin_comment
comment|/**  * A HBase ReplicationLoad to present MetricsSource information  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|ReplicationLoadSource
block|{
specifier|private
name|String
name|peerID
decl_stmt|;
specifier|private
name|long
name|ageOfLastShippedOp
decl_stmt|;
specifier|private
name|int
name|sizeOfLogQueue
decl_stmt|;
specifier|private
name|long
name|timeStampOfLastShippedOp
decl_stmt|;
specifier|private
name|long
name|replicationLag
decl_stmt|;
specifier|public
name|ReplicationLoadSource
parameter_list|(
name|String
name|id
parameter_list|,
name|long
name|age
parameter_list|,
name|int
name|size
parameter_list|,
name|long
name|timeStamp
parameter_list|,
name|long
name|lag
parameter_list|)
block|{
name|this
operator|.
name|peerID
operator|=
name|id
expr_stmt|;
name|this
operator|.
name|ageOfLastShippedOp
operator|=
name|age
expr_stmt|;
name|this
operator|.
name|sizeOfLogQueue
operator|=
name|size
expr_stmt|;
name|this
operator|.
name|timeStampOfLastShippedOp
operator|=
name|timeStamp
expr_stmt|;
name|this
operator|.
name|replicationLag
operator|=
name|lag
expr_stmt|;
block|}
specifier|public
name|String
name|getPeerID
parameter_list|()
block|{
return|return
name|this
operator|.
name|peerID
return|;
block|}
specifier|public
name|long
name|getAgeOfLastShippedOp
parameter_list|()
block|{
return|return
name|this
operator|.
name|ageOfLastShippedOp
return|;
block|}
specifier|public
name|long
name|getSizeOfLogQueue
parameter_list|()
block|{
return|return
name|this
operator|.
name|sizeOfLogQueue
return|;
block|}
specifier|public
name|long
name|getTimeStampOfLastShippedOp
parameter_list|()
block|{
return|return
name|this
operator|.
name|timeStampOfLastShippedOp
return|;
block|}
specifier|public
name|long
name|getReplicationLag
parameter_list|()
block|{
return|return
name|this
operator|.
name|replicationLag
return|;
block|}
block|}
end_class

end_unit

