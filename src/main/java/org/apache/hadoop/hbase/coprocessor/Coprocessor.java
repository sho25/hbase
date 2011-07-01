begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Copyright 2010 The Apache Software Foundation  *  * Licensed under the Apache License, Version 2.0 (the "License");  * you may not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|coprocessor
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

begin_comment
comment|/**  * Coprocess interface.  */
end_comment

begin_interface
specifier|public
interface|interface
name|Coprocessor
block|{
specifier|static
specifier|final
name|int
name|VERSION
init|=
literal|1
decl_stmt|;
comment|/** Highest installation priority */
specifier|static
specifier|final
name|int
name|PRIORITY_HIGHEST
init|=
literal|0
decl_stmt|;
comment|/** High (system) installation priority */
specifier|static
specifier|final
name|int
name|PRIORITY_SYSTEM
init|=
name|Integer
operator|.
name|MAX_VALUE
operator|/
literal|4
decl_stmt|;
comment|/** Default installation priority for user coprocessors */
specifier|static
specifier|final
name|int
name|PRIORITY_USER
init|=
name|Integer
operator|.
name|MAX_VALUE
operator|/
literal|2
decl_stmt|;
comment|/** Lowest installation priority */
specifier|static
specifier|final
name|int
name|PRIORITY_LOWEST
init|=
name|Integer
operator|.
name|MAX_VALUE
decl_stmt|;
comment|/**    * Lifecycle state of a given coprocessor instance.    */
specifier|public
enum|enum
name|State
block|{
name|UNINSTALLED
block|,
name|INSTALLED
block|,
name|STARTING
block|,
name|ACTIVE
block|,
name|STOPPING
block|,
name|STOPPED
block|}
comment|// Interface
name|void
name|start
parameter_list|(
name|CoprocessorEnvironment
name|env
parameter_list|)
throws|throws
name|IOException
function_decl|;
name|void
name|stop
parameter_list|(
name|CoprocessorEnvironment
name|env
parameter_list|)
throws|throws
name|IOException
function_decl|;
block|}
end_interface

end_unit

