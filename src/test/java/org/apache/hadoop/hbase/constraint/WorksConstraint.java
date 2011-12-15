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
name|constraint
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
name|client
operator|.
name|Put
import|;
end_import

begin_comment
comment|/**  * It just works  */
end_comment

begin_class
specifier|public
class|class
name|WorksConstraint
extends|extends
name|BaseConstraint
block|{
annotation|@
name|Override
specifier|public
name|void
name|check
parameter_list|(
name|Put
name|p
parameter_list|)
block|{
comment|// NOOP
block|}
comment|/**    * Constraint to check that the naming of constraints doesn't mess up the    * pattern matching.(that constraint $___Constraint$NameConstraint isn't a    * problem)    */
specifier|public
specifier|static
class|class
name|NameConstraint
extends|extends
name|WorksConstraint
block|{   }
block|}
end_class

end_unit

