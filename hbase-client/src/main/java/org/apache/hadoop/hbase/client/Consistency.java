begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements. See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License. You may obtain a copy of the License at  *  * http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|client
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
name|InterfaceStability
import|;
end_import

begin_comment
comment|/**  * Consistency defines the expected consistency level for an operation.  */
end_comment

begin_enum
annotation|@
name|InterfaceAudience
operator|.
name|Public
annotation|@
name|InterfaceStability
operator|.
name|Evolving
specifier|public
enum|enum
name|Consistency
block|{
comment|// developer note: Do not reorder. Client.proto#Consistency depends on this order
comment|/**    * Strong consistency is the default consistency model in HBase,    * where reads and writes go through a single server which serializes    * the updates, and returns all data that was written and ack'd.    */
name|STRONG
block|,
comment|/**    * Timeline consistent reads might return values that may not see    * the most recent updates. Write transactions are always performed    * in strong consistency model in HBase which guarantees that transactions    * are ordered, and replayed in the same order by all copies of the data.    * In timeline consistency, the get and scan requests can be answered from data    * that may be stale.    *<br>    * The client may still observe transactions out of order if the requests are    * responded from different servers.    */
name|TIMELINE
block|, }
end_enum

end_unit

