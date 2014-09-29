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
comment|/**  * Using this Interface one can mark a Cell as timestamp changeable.<br>  * Note : Server side Cell implementations in write path must implement this.  */
end_comment

begin_interface
annotation|@
name|InterfaceAudience
operator|.
name|LimitedPrivate
argument_list|(
name|HBaseInterfaceAudience
operator|.
name|COPROC
argument_list|)
specifier|public
interface|interface
name|SettableTimestamp
block|{
comment|/**    * Sets with the given timestamp.    * @param ts    */
name|void
name|setTimestamp
parameter_list|(
name|long
name|ts
parameter_list|)
function_decl|;
comment|/**    * Sets with the given timestamp.    * @param ts buffer containing the timestamp value    * @param tsOffset offset to the new timestamp    */
name|void
name|setTimestamp
parameter_list|(
name|byte
index|[]
name|ts
parameter_list|,
name|int
name|tsOffset
parameter_list|)
function_decl|;
block|}
end_interface

end_unit

