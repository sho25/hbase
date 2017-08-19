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
name|client
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
comment|/**  * Currently, there are only two compact types:  * {@code NORMAL} means do store files compaction;  * {@code MOB} means do mob files compaction.  * */
end_comment

begin_enum
annotation|@
name|InterfaceAudience
operator|.
name|Public
specifier|public
enum|enum
name|CompactType
block|{
name|NORMAL
argument_list|(
literal|0
argument_list|)
block|,
name|MOB
argument_list|(
literal|1
argument_list|)
block|;
name|CompactType
parameter_list|(
name|int
name|value
parameter_list|)
block|{}
block|}
end_enum

end_unit

