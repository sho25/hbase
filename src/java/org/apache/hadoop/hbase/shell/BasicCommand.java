begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Copyright 2007 The Apache Software Foundation  *  * Licensed under the Apache License, Version 2.0 (the "License");  * you may not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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

begin_comment
comment|/**  * @see<a href="http://wiki.apache.org/lucene-hadoop/Hbase/HbaseShell">HBaseShell</a>  */
end_comment

begin_class
specifier|public
specifier|abstract
class|class
name|BasicCommand
implements|implements
name|Command
implements|,
name|CommandFactory
block|{
specifier|public
name|BasicCommand
name|getBasicCommand
parameter_list|()
block|{
return|return
name|this
return|;
block|}
comment|/** basic commands are their own factories. */
specifier|public
name|Command
name|getCommand
parameter_list|()
block|{
return|return
name|this
return|;
block|}
block|}
end_class

end_unit

