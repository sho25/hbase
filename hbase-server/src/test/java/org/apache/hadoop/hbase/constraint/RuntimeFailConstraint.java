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
name|exceptions
operator|.
name|ConstraintException
import|;
end_import

begin_comment
comment|/**  * Always non-gracefully fail on attempt  */
end_comment

begin_class
specifier|public
class|class
name|RuntimeFailConstraint
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
throws|throws
name|ConstraintException
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"RuntimeFailConstraint always throws a runtime exception"
argument_list|)
throw|;
block|}
block|}
end_class

end_unit

