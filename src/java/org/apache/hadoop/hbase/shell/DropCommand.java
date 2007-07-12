begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Copyright 2007 The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|shell
package|;
end_package

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|IOException
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
name|HClient
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

begin_class
specifier|public
class|class
name|DropCommand
extends|extends
name|BasicCommand
block|{
name|String
name|argument
decl_stmt|;
specifier|public
name|ReturnMsg
name|execute
parameter_list|(
name|HClient
name|client
parameter_list|)
block|{
if|if
condition|(
name|this
operator|.
name|argument
operator|==
literal|null
condition|)
return|return
operator|new
name|ReturnMsg
argument_list|(
literal|0
argument_list|,
literal|"Syntax error : Please check 'Drop' syntax."
argument_list|)
return|;
try|try
block|{
name|client
operator|.
name|deleteTable
argument_list|(
operator|new
name|Text
argument_list|(
name|this
operator|.
name|argument
argument_list|)
argument_list|)
expr_stmt|;
return|return
operator|new
name|ReturnMsg
argument_list|(
literal|1
argument_list|,
literal|"Table droped successfully."
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
return|return
operator|new
name|ReturnMsg
argument_list|(
literal|0
argument_list|,
literal|"error msg : "
operator|+
name|e
operator|.
name|toString
argument_list|()
argument_list|)
return|;
block|}
block|}
specifier|public
name|void
name|setArgument
parameter_list|(
name|String
name|argument
parameter_list|)
block|{
name|this
operator|.
name|argument
operator|=
name|argument
expr_stmt|;
block|}
block|}
end_class

end_unit

