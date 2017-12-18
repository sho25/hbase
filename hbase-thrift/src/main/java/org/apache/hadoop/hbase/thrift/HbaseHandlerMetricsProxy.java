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
name|thrift
package|;
end_package

begin_import
import|import
name|java
operator|.
name|lang
operator|.
name|reflect
operator|.
name|InvocationHandler
import|;
end_import

begin_import
import|import
name|java
operator|.
name|lang
operator|.
name|reflect
operator|.
name|InvocationTargetException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|lang
operator|.
name|reflect
operator|.
name|Method
import|;
end_import

begin_import
import|import
name|java
operator|.
name|lang
operator|.
name|reflect
operator|.
name|Proxy
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
name|conf
operator|.
name|Configuration
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
name|thrift
operator|.
name|generated
operator|.
name|Hbase
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|yetus
operator|.
name|audience
operator|.
name|InterfaceAudience
import|;
end_import

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|Logger
import|;
end_import

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|LoggerFactory
import|;
end_import

begin_comment
comment|/**  * Converts a Hbase.Iface using InvocationHandler so that it reports process  * time of each call to ThriftMetrics.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|HbaseHandlerMetricsProxy
implements|implements
name|InvocationHandler
block|{
specifier|private
specifier|static
specifier|final
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|HbaseHandlerMetricsProxy
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|Hbase
operator|.
name|Iface
name|handler
decl_stmt|;
specifier|private
specifier|final
name|ThriftMetrics
name|metrics
decl_stmt|;
specifier|public
specifier|static
name|Hbase
operator|.
name|Iface
name|newInstance
parameter_list|(
name|Hbase
operator|.
name|Iface
name|handler
parameter_list|,
name|ThriftMetrics
name|metrics
parameter_list|,
name|Configuration
name|conf
parameter_list|)
block|{
return|return
operator|(
name|Hbase
operator|.
name|Iface
operator|)
name|Proxy
operator|.
name|newProxyInstance
argument_list|(
name|handler
operator|.
name|getClass
argument_list|()
operator|.
name|getClassLoader
argument_list|()
argument_list|,
operator|new
name|Class
index|[]
block|{
name|Hbase
operator|.
name|Iface
operator|.
name|class
block|}
argument_list|,
operator|new
name|HbaseHandlerMetricsProxy
argument_list|(
name|handler
argument_list|,
name|metrics
argument_list|,
name|conf
argument_list|)
argument_list|)
return|;
block|}
specifier|private
name|HbaseHandlerMetricsProxy
parameter_list|(
name|Hbase
operator|.
name|Iface
name|handler
parameter_list|,
name|ThriftMetrics
name|metrics
parameter_list|,
name|Configuration
name|conf
parameter_list|)
block|{
name|this
operator|.
name|handler
operator|=
name|handler
expr_stmt|;
name|this
operator|.
name|metrics
operator|=
name|metrics
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|Object
name|invoke
parameter_list|(
name|Object
name|proxy
parameter_list|,
name|Method
name|m
parameter_list|,
name|Object
index|[]
name|args
parameter_list|)
throws|throws
name|Throwable
block|{
name|Object
name|result
decl_stmt|;
name|long
name|start
init|=
name|now
argument_list|()
decl_stmt|;
try|try
block|{
name|result
operator|=
name|m
operator|.
name|invoke
argument_list|(
name|handler
argument_list|,
name|args
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InvocationTargetException
name|e
parameter_list|)
block|{
name|metrics
operator|.
name|exception
argument_list|(
name|e
operator|.
name|getCause
argument_list|()
argument_list|)
expr_stmt|;
throw|throw
name|e
operator|.
name|getTargetException
argument_list|()
throw|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|metrics
operator|.
name|exception
argument_list|(
name|e
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"unexpected invocation exception: "
operator|+
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
throw|;
block|}
finally|finally
block|{
name|long
name|processTime
init|=
name|now
argument_list|()
operator|-
name|start
decl_stmt|;
name|metrics
operator|.
name|incMethodTime
argument_list|(
name|m
operator|.
name|getName
argument_list|()
argument_list|,
name|processTime
argument_list|)
expr_stmt|;
block|}
return|return
name|result
return|;
block|}
specifier|private
specifier|static
name|long
name|now
parameter_list|()
block|{
return|return
name|System
operator|.
name|nanoTime
argument_list|()
return|;
block|}
block|}
end_class

end_unit

