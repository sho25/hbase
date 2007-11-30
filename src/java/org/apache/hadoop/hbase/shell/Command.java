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
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|HBaseConfiguration
import|;
end_import

begin_interface
specifier|public
interface|interface
name|Command
block|{
comment|/** family indicator */
specifier|public
specifier|static
specifier|final
name|String
name|FAMILY_INDICATOR
init|=
literal|":"
decl_stmt|;
specifier|public
enum|enum
name|CommandType
block|{
name|DDL
block|,
name|UPDATE
block|,
name|SELECT
block|,
name|INSERT
block|,
name|DELETE
block|,
name|SHELL
block|}
comment|/**    * Execute a command    *     * @param conf Configuration    * @return Result of command execution    */
specifier|public
name|ReturnMsg
name|execute
parameter_list|(
specifier|final
name|HBaseConfiguration
name|conf
parameter_list|)
function_decl|;
comment|/**    * @return Type of this command whether DDL, SELECT, INSERT, UPDATE, DELETE,    *         or SHELL.    */
specifier|public
name|CommandType
name|getCommandType
parameter_list|()
function_decl|;
block|}
end_interface

end_unit

