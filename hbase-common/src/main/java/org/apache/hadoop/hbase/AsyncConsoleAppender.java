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

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|log4j
operator|.
name|AsyncAppender
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|log4j
operator|.
name|ConsoleAppender
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|log4j
operator|.
name|PatternLayout
import|;
end_import

begin_comment
comment|/**  * Logger class that buffers before trying to log to the specified console.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|AsyncConsoleAppender
extends|extends
name|AsyncAppender
block|{
specifier|private
specifier|final
name|ConsoleAppender
name|consoleAppender
decl_stmt|;
specifier|public
name|AsyncConsoleAppender
parameter_list|()
block|{
name|super
argument_list|()
expr_stmt|;
name|consoleAppender
operator|=
operator|new
name|ConsoleAppender
argument_list|(
operator|new
name|PatternLayout
argument_list|(
literal|"%d{ISO8601} %-5p [%t] %c{2}: %m%n"
argument_list|)
argument_list|)
expr_stmt|;
name|this
operator|.
name|addAppender
argument_list|(
name|consoleAppender
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|setTarget
parameter_list|(
name|String
name|value
parameter_list|)
block|{
name|consoleAppender
operator|.
name|setTarget
argument_list|(
name|value
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|activateOptions
parameter_list|()
block|{
name|consoleAppender
operator|.
name|activateOptions
argument_list|()
expr_stmt|;
name|super
operator|.
name|activateOptions
argument_list|()
expr_stmt|;
block|}
block|}
end_class

end_unit

