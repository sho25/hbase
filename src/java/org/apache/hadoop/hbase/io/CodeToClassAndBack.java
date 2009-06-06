begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Copyright 2009 The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|io
package|;
end_package

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|*
import|;
end_import

begin_comment
comment|/**  * A Static Interface.  * Instead of having this code in the the HbaseMapWritable code, where it  * blocks the possibility of altering the variables and changing their types,  * it is put here in this static interface where the static final Maps are  * loaded one time. Only byte[] and Cell are supported at this time.  */
end_comment

begin_interface
specifier|public
interface|interface
name|CodeToClassAndBack
block|{
comment|/**    * Static map that contains mapping from code to class    */
specifier|public
specifier|static
specifier|final
name|Map
argument_list|<
name|Byte
argument_list|,
name|Class
argument_list|<
name|?
argument_list|>
argument_list|>
name|CODE_TO_CLASS
init|=
operator|new
name|HashMap
argument_list|<
name|Byte
argument_list|,
name|Class
argument_list|<
name|?
argument_list|>
argument_list|>
argument_list|()
decl_stmt|;
comment|/**    * Static map that contains mapping from class to code    */
specifier|public
specifier|static
specifier|final
name|Map
argument_list|<
name|Class
argument_list|<
name|?
argument_list|>
argument_list|,
name|Byte
argument_list|>
name|CLASS_TO_CODE
init|=
operator|new
name|HashMap
argument_list|<
name|Class
argument_list|<
name|?
argument_list|>
argument_list|,
name|Byte
argument_list|>
argument_list|()
decl_stmt|;
comment|/**    * Class list for supported classes    */
specifier|public
name|Class
argument_list|<
name|?
argument_list|>
index|[]
name|classList
init|=
block|{
name|byte
index|[]
operator|.
name|class
block|,
name|Cell
operator|.
name|class
block|}
decl_stmt|;
comment|/**    * The static loader that is used instead of the static constructor in    * HbaseMapWritable.    */
specifier|public
name|InternalStaticLoader
name|sl
init|=
operator|new
name|InternalStaticLoader
argument_list|(
name|classList
argument_list|,
name|CODE_TO_CLASS
argument_list|,
name|CLASS_TO_CODE
argument_list|)
decl_stmt|;
comment|/**    * Class that loads the static maps with their values.     */
specifier|public
class|class
name|InternalStaticLoader
block|{
name|InternalStaticLoader
parameter_list|(
name|Class
argument_list|<
name|?
argument_list|>
index|[]
name|classList
parameter_list|,
name|Map
argument_list|<
name|Byte
argument_list|,
name|Class
argument_list|<
name|?
argument_list|>
argument_list|>
name|CODE_TO_CLASS
parameter_list|,
name|Map
argument_list|<
name|Class
argument_list|<
name|?
argument_list|>
argument_list|,
name|Byte
argument_list|>
name|CLASS_TO_CODE
parameter_list|)
block|{
name|byte
name|code
init|=
literal|1
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|classList
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|CLASS_TO_CODE
operator|.
name|put
argument_list|(
name|classList
index|[
name|i
index|]
argument_list|,
name|code
argument_list|)
expr_stmt|;
name|CODE_TO_CLASS
operator|.
name|put
argument_list|(
name|code
argument_list|,
name|classList
index|[
name|i
index|]
argument_list|)
expr_stmt|;
name|code
operator|++
expr_stmt|;
block|}
block|}
block|}
block|}
end_interface

end_unit

