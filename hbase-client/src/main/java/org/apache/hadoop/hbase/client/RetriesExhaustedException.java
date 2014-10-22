begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  *  * Licensed under the Apache License, Version 2.0 (the "License");  * you may not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|client
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
name|java
operator|.
name|util
operator|.
name|Date
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|List
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
name|hadoop
operator|.
name|hbase
operator|.
name|classification
operator|.
name|InterfaceStability
import|;
end_import

begin_comment
comment|/**  * Exception thrown by HTable methods when an attempt to do something (like  * commit changes) fails after a bunch of retries.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Public
annotation|@
name|InterfaceStability
operator|.
name|Stable
specifier|public
class|class
name|RetriesExhaustedException
extends|extends
name|IOException
block|{
specifier|private
specifier|static
specifier|final
name|long
name|serialVersionUID
init|=
literal|1876775844L
decl_stmt|;
specifier|public
name|RetriesExhaustedException
parameter_list|(
specifier|final
name|String
name|msg
parameter_list|)
block|{
name|super
argument_list|(
name|msg
argument_list|)
expr_stmt|;
block|}
specifier|public
name|RetriesExhaustedException
parameter_list|(
specifier|final
name|String
name|msg
parameter_list|,
specifier|final
name|IOException
name|e
parameter_list|)
block|{
name|super
argument_list|(
name|msg
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
comment|/**    * Datastructure that allows adding more info around Throwable incident.    */
specifier|public
specifier|static
class|class
name|ThrowableWithExtraContext
block|{
specifier|private
specifier|final
name|Throwable
name|t
decl_stmt|;
specifier|private
specifier|final
name|long
name|when
decl_stmt|;
specifier|private
specifier|final
name|String
name|extras
decl_stmt|;
specifier|public
name|ThrowableWithExtraContext
parameter_list|(
specifier|final
name|Throwable
name|t
parameter_list|,
specifier|final
name|long
name|when
parameter_list|,
specifier|final
name|String
name|extras
parameter_list|)
block|{
name|this
operator|.
name|t
operator|=
name|t
expr_stmt|;
name|this
operator|.
name|when
operator|=
name|when
expr_stmt|;
name|this
operator|.
name|extras
operator|=
name|extras
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
operator|new
name|Date
argument_list|(
name|this
operator|.
name|when
argument_list|)
operator|.
name|toString
argument_list|()
operator|+
literal|", "
operator|+
name|extras
operator|+
literal|", "
operator|+
name|t
operator|.
name|toString
argument_list|()
return|;
block|}
block|}
comment|/**    * Create a new RetriesExhaustedException from the list of prior failures.    * @param callableVitals Details from the Callable we were using    * when we got this exception.    * @param numTries The number of tries we made    * @param exceptions List of exceptions that failed before giving up    */
specifier|public
name|RetriesExhaustedException
parameter_list|(
specifier|final
name|String
name|callableVitals
parameter_list|,
name|int
name|numTries
parameter_list|,
name|List
argument_list|<
name|Throwable
argument_list|>
name|exceptions
parameter_list|)
block|{
name|super
argument_list|(
name|getMessage
argument_list|(
name|callableVitals
argument_list|,
name|numTries
argument_list|,
name|exceptions
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|/**    * Create a new RetriesExhaustedException from the list of prior failures.    * @param numTries    * @param exceptions List of exceptions that failed before giving up    */
specifier|public
name|RetriesExhaustedException
parameter_list|(
specifier|final
name|int
name|numTries
parameter_list|,
specifier|final
name|List
argument_list|<
name|ThrowableWithExtraContext
argument_list|>
name|exceptions
parameter_list|)
block|{
name|super
argument_list|(
name|getMessage
argument_list|(
name|numTries
argument_list|,
name|exceptions
argument_list|)
argument_list|,
operator|(
name|exceptions
operator|!=
literal|null
operator|&&
operator|!
name|exceptions
operator|.
name|isEmpty
argument_list|()
condition|?
name|exceptions
operator|.
name|get
argument_list|(
name|exceptions
operator|.
name|size
argument_list|()
operator|-
literal|1
argument_list|)
operator|.
name|t
else|:
literal|null
operator|)
argument_list|)
expr_stmt|;
block|}
specifier|private
specifier|static
name|String
name|getMessage
parameter_list|(
name|String
name|callableVitals
parameter_list|,
name|int
name|numTries
parameter_list|,
name|List
argument_list|<
name|Throwable
argument_list|>
name|exceptions
parameter_list|)
block|{
name|StringBuilder
name|buffer
init|=
operator|new
name|StringBuilder
argument_list|(
literal|"Failed contacting "
argument_list|)
decl_stmt|;
name|buffer
operator|.
name|append
argument_list|(
name|callableVitals
argument_list|)
expr_stmt|;
name|buffer
operator|.
name|append
argument_list|(
literal|" after "
argument_list|)
expr_stmt|;
name|buffer
operator|.
name|append
argument_list|(
name|numTries
operator|+
literal|1
argument_list|)
expr_stmt|;
name|buffer
operator|.
name|append
argument_list|(
literal|" attempts.\nExceptions:\n"
argument_list|)
expr_stmt|;
for|for
control|(
name|Throwable
name|t
range|:
name|exceptions
control|)
block|{
name|buffer
operator|.
name|append
argument_list|(
name|t
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|buffer
operator|.
name|append
argument_list|(
literal|"\n"
argument_list|)
expr_stmt|;
block|}
return|return
name|buffer
operator|.
name|toString
argument_list|()
return|;
block|}
specifier|private
specifier|static
name|String
name|getMessage
parameter_list|(
specifier|final
name|int
name|numTries
parameter_list|,
specifier|final
name|List
argument_list|<
name|ThrowableWithExtraContext
argument_list|>
name|exceptions
parameter_list|)
block|{
name|StringBuilder
name|buffer
init|=
operator|new
name|StringBuilder
argument_list|(
literal|"Failed after attempts="
argument_list|)
decl_stmt|;
name|buffer
operator|.
name|append
argument_list|(
name|numTries
operator|+
literal|1
argument_list|)
expr_stmt|;
name|buffer
operator|.
name|append
argument_list|(
literal|", exceptions:\n"
argument_list|)
expr_stmt|;
for|for
control|(
name|ThrowableWithExtraContext
name|t
range|:
name|exceptions
control|)
block|{
name|buffer
operator|.
name|append
argument_list|(
name|t
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|buffer
operator|.
name|append
argument_list|(
literal|"\n"
argument_list|)
expr_stmt|;
block|}
return|return
name|buffer
operator|.
name|toString
argument_list|()
return|;
block|}
block|}
end_class

end_unit

