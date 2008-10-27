begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Copyright 2008 The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|util
package|;
end_package

begin_import
import|import
name|java
operator|.
name|lang
operator|.
name|ref
operator|.
name|ReferenceQueue
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Map
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|logging
operator|.
name|Log
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|logging
operator|.
name|LogFactory
import|;
end_import

begin_comment
comment|/**  * java.lang.ref.ReferenceQueue utility class.  * @param<K>  * @param<V>  */
end_comment

begin_class
class|class
name|ReferenceQueueUtil
parameter_list|<
name|K
parameter_list|,
name|V
parameter_list|>
block|{
specifier|private
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|this
operator|.
name|getClass
argument_list|()
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|ReferenceQueue
name|rq
init|=
operator|new
name|ReferenceQueue
argument_list|()
decl_stmt|;
specifier|private
specifier|final
name|Map
argument_list|<
name|K
argument_list|,
name|V
argument_list|>
name|map
decl_stmt|;
specifier|private
name|ReferenceQueueUtil
parameter_list|()
block|{
name|this
argument_list|(
literal|null
argument_list|)
expr_stmt|;
block|}
name|ReferenceQueueUtil
parameter_list|(
specifier|final
name|Map
argument_list|<
name|K
argument_list|,
name|V
argument_list|>
name|m
parameter_list|)
block|{
name|super
argument_list|()
expr_stmt|;
name|this
operator|.
name|map
operator|=
name|m
expr_stmt|;
block|}
specifier|public
name|ReferenceQueue
name|getReferenceQueue
parameter_list|()
block|{
return|return
name|rq
return|;
block|}
comment|/**    * Check the reference queue and delete anything that has since gone away    */
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
name|void
name|checkReferences
parameter_list|()
block|{
name|int
name|i
init|=
literal|0
decl_stmt|;
for|for
control|(
name|Object
name|obj
init|=
literal|null
init|;
operator|(
name|obj
operator|=
name|this
operator|.
name|rq
operator|.
name|poll
argument_list|()
operator|)
operator|!=
literal|null
condition|;
control|)
block|{
name|i
operator|++
expr_stmt|;
name|this
operator|.
name|map
operator|.
name|remove
argument_list|(
operator|(
operator|(
name|SoftValue
argument_list|<
name|K
argument_list|,
name|V
argument_list|>
operator|)
name|obj
operator|)
operator|.
name|getKey
argument_list|()
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|i
operator|>
literal|0
operator|&&
name|LOG
operator|.
name|isDebugEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|""
operator|+
name|i
operator|+
literal|" reference(s) cleared."
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

