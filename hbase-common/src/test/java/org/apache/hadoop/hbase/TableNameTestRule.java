begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|junit
operator|.
name|rules
operator|.
name|TestWatcher
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|runner
operator|.
name|Description
import|;
end_import

begin_comment
comment|/**  * Returns a {@code TableName} based on currently running test method name.  */
end_comment

begin_class
specifier|public
class|class
name|TableNameTestRule
extends|extends
name|TestWatcher
block|{
specifier|private
name|TableName
name|tableName
decl_stmt|;
annotation|@
name|Override
specifier|protected
name|void
name|starting
parameter_list|(
name|Description
name|description
parameter_list|)
block|{
name|tableName
operator|=
name|TableName
operator|.
name|valueOf
argument_list|(
name|description
operator|.
name|getMethodName
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|public
name|TableName
name|getTableName
parameter_list|()
block|{
return|return
name|tableName
return|;
block|}
block|}
end_class

end_unit

