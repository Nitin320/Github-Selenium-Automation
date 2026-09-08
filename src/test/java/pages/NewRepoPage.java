package pages;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

public class NewRepoPage {
    WebDriver driver;

    // Constructor
    public NewRepoPage(WebDriver driver) {
        this.driver = driver;
        PageFactory.initElements(driver, this);
    }

    // Element Locator for Repository Name input field
    @FindBy(id = "repository_name")
    private WebElement repoNameInput;

    // Element Locator for "Create repository" submit button
    @FindBy(css = "button[type='submit'].first-in-line")
    private WebElement createRepoButton;

    // Action methods
    public void enterRepoName(String repoName) {
        repoNameInput.sendKeys(repoName);
    }

    public void clickCreateRepository() {
        createRepoButton.click();
    }
}