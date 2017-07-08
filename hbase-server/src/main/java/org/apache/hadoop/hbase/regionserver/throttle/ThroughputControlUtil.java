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
name|regionserver
operator|.
name|throttle
package|;
end_package

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|atomic
operator|.
name|AtomicInteger
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
name|regionserver
operator|.
name|Store
import|;
end_import

begin_comment
comment|/**  * Helper methods for throttling  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
specifier|final
class|class
name|ThroughputControlUtil
block|{
specifier|private
name|ThroughputControlUtil
parameter_list|()
block|{   }
specifier|private
specifier|static
specifier|final
name|AtomicInteger
name|NAME_COUNTER
init|=
operator|new
name|AtomicInteger
argument_list|(
literal|0
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|NAME_DELIMITER
init|=
literal|"#"
decl_stmt|;
comment|/**    * Generate a name for throttling, to prevent name conflict when multiple IO operation running    * parallel on the same store.    * @param store the Store instance on which IO operation is happening    * @param opName Name of the IO operation, e.g. "flush", "compaction", etc.    * @return The name for throttling    */
specifier|public
specifier|static
name|String
name|getNameForThrottling
parameter_list|(
specifier|final
name|Store
name|store
parameter_list|,
specifier|final
name|String
name|opName
parameter_list|)
block|{
name|int
name|counter
decl_stmt|;
for|for
control|(
init|;
condition|;
control|)
block|{
name|counter
operator|=
name|NAME_COUNTER
operator|.
name|get
argument_list|()
expr_stmt|;
name|int
name|next
init|=
name|counter
operator|==
name|Integer
operator|.
name|MAX_VALUE
condition|?
literal|0
else|:
name|counter
operator|+
literal|1
decl_stmt|;
if|if
condition|(
name|NAME_COUNTER
operator|.
name|compareAndSet
argument_list|(
name|counter
argument_list|,
name|next
argument_list|)
condition|)
block|{
break|break;
block|}
block|}
return|return
name|store
operator|.
name|getRegionInfo
argument_list|()
operator|.
name|getRegionNameAsString
argument_list|()
operator|+
name|NAME_DELIMITER
operator|+
name|store
operator|.
name|getColumnFamilyDescriptor
argument_list|()
operator|.
name|getNameAsString
argument_list|()
operator|+
name|NAME_DELIMITER
operator|+
name|opName
operator|+
name|NAME_DELIMITER
operator|+
name|counter
return|;
block|}
block|}
end_class

end_unit

