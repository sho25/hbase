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
package|;
end_package

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

begin_comment
comment|/**  * Enum describing all possible memory compaction policies  */
end_comment

begin_enum
annotation|@
name|InterfaceAudience
operator|.
name|Public
specifier|public
enum|enum
name|MemoryCompactionPolicy
block|{
comment|/**    * No memory compaction, when size threshold is exceeded data is flushed to disk    */
name|NONE
block|,
comment|/**    * Basic policy applies optimizations which modify the index to a more compacted representation.    * This is beneficial in all access patterns. The smaller the cells are the greater the    * benefit of this policy.    * This is the default policy.    */
name|BASIC
block|,
comment|/**    * In addition to compacting the index representation as the basic policy, eager policy    * eliminates duplication while the data is still in memory (much like the    * on-disk compaction does after the data is flushed to disk). This policy is most useful for    * applications with high data churn or small working sets.    */
name|EAGER
block|}
end_enum

end_unit

