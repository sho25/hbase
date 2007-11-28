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
operator|.
name|algebra
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
name|HStoreKey
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
name|mapred
operator|.
name|IdentityTableMap
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
name|mapred
operator|.
name|TableMap
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
name|mapred
operator|.
name|TableOutputCollector
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
name|shell
operator|.
name|algebra
operator|.
name|generated
operator|.
name|ExpressionParser
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
name|shell
operator|.
name|algebra
operator|.
name|generated
operator|.
name|ParseException
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
name|MapWritable
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

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|mapred
operator|.
name|JobConf
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
name|mapred
operator|.
name|Reporter
import|;
end_import

begin_comment
comment|/**  * Extract filtered records.  */
end_comment

begin_class
specifier|public
class|class
name|IdentityFilterMap
extends|extends
name|IdentityTableMap
block|{
name|ExpressionParser
name|expressionParser
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|EXPRESSION
init|=
literal|"shell.mapred.filtertablemap.exps"
decl_stmt|;
annotation|@
name|SuppressWarnings
argument_list|(
literal|"deprecation"
argument_list|)
specifier|public
specifier|static
name|void
name|initJob
parameter_list|(
name|String
name|table
parameter_list|,
name|String
name|columns
parameter_list|,
name|String
name|expression
parameter_list|,
name|Class
argument_list|<
name|?
extends|extends
name|TableMap
argument_list|>
name|mapper
parameter_list|,
name|JobConf
name|job
parameter_list|)
block|{
name|initJob
argument_list|(
name|table
argument_list|,
name|columns
argument_list|,
name|mapper
argument_list|,
name|job
argument_list|)
expr_stmt|;
name|job
operator|.
name|set
argument_list|(
name|EXPRESSION
argument_list|,
name|expression
argument_list|)
expr_stmt|;
block|}
comment|/*    * (non-Javadoc)    *     * @see org.apache.hadoop.hbase.mapred.TableMap#configure(org.apache.hadoop.mapred.JobConf)    */
specifier|public
name|void
name|configure
parameter_list|(
name|JobConf
name|job
parameter_list|)
block|{
name|super
operator|.
name|configure
argument_list|(
name|job
argument_list|)
expr_stmt|;
name|expressionParser
operator|=
operator|new
name|ExpressionParser
argument_list|(
name|job
operator|.
name|get
argument_list|(
name|EXPRESSION
argument_list|,
literal|""
argument_list|)
argument_list|)
expr_stmt|;
try|try
block|{
name|expressionParser
operator|.
name|booleanExpressionParse
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|ParseException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
comment|/**    * Filter the value for each specified column family.    */
specifier|public
name|void
name|map
parameter_list|(
name|HStoreKey
name|key
parameter_list|,
name|MapWritable
name|value
parameter_list|,
name|TableOutputCollector
name|output
parameter_list|,
name|Reporter
name|reporter
parameter_list|)
throws|throws
name|IOException
block|{
name|Text
name|tKey
init|=
name|key
operator|.
name|getRow
argument_list|()
decl_stmt|;
try|try
block|{
if|if
condition|(
name|expressionParser
operator|.
name|checkConstraints
argument_list|(
name|value
argument_list|)
condition|)
block|{
name|output
operator|.
name|collect
argument_list|(
name|tKey
argument_list|,
name|value
argument_list|)
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|ParseException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
block|}
end_class

end_unit

