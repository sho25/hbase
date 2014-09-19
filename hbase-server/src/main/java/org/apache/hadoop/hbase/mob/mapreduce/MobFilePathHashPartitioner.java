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
name|mob
operator|.
name|mapreduce
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
name|classification
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
name|KeyValue
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
name|mob
operator|.
name|MobFileName
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
name|io
operator|.
name|Text
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
name|mapreduce
operator|.
name|Partitioner
import|;
end_import

begin_comment
comment|/**  * The partitioner for the sweep job.  * The key is a mob file name. We bucket by date.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|MobFilePathHashPartitioner
extends|extends
name|Partitioner
argument_list|<
name|Text
argument_list|,
name|KeyValue
argument_list|>
block|{
annotation|@
name|Override
specifier|public
name|int
name|getPartition
parameter_list|(
name|Text
name|fileName
parameter_list|,
name|KeyValue
name|kv
parameter_list|,
name|int
name|numPartitions
parameter_list|)
block|{
name|MobFileName
name|mobFileName
init|=
name|MobFileName
operator|.
name|create
argument_list|(
name|fileName
operator|.
name|toString
argument_list|()
argument_list|)
decl_stmt|;
name|String
name|date
init|=
name|mobFileName
operator|.
name|getDate
argument_list|()
decl_stmt|;
name|int
name|hash
init|=
name|date
operator|.
name|hashCode
argument_list|()
decl_stmt|;
return|return
operator|(
name|hash
operator|&
name|Integer
operator|.
name|MAX_VALUE
operator|)
operator|%
name|numPartitions
return|;
block|}
block|}
end_class

end_unit

